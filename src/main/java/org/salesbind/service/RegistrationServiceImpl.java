package org.salesbind.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.salesbind.dto.CompleteRegistrationRequest;
import org.salesbind.exception.EmailAlreadyExistsException;
import org.salesbind.exception.EmailNotVerifiedException;
import org.salesbind.exception.InvalidRegistrationSessionException;
import org.salesbind.exception.InvalidVerificationCode;
import org.salesbind.exception.VerificationCodeMaxAttemptsExceededException;
import org.salesbind.infrastructure.configuration.RegistrationProperties;
import org.salesbind.infrastructure.security.PasswordEncoder;
import org.salesbind.infrastructure.security.RandomNumericCodeGenerator;
import org.salesbind.infrastructure.session.SessionIdGenerator;
import org.salesbind.model.RegistrationAttempt;
import org.salesbind.model.User;
import org.salesbind.repository.RegistrationAttemptRepository;
import org.salesbind.repository.UserRepository;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegistrationServiceImpl implements RegistrationService {

    public static final int VERIFICATION_CODE_LENGTH = 6;

    @Inject
    RegistrationAttemptRepository registrationAttemptRepository;

    @Inject
    SessionIdGenerator sessionIdGenerator;

    @Inject
    RandomNumericCodeGenerator randomNumericCodeGenerator;

    @Inject
    PasswordEncoder passwordEncoder;

    @Inject
    UserRepository userRepository;

    @Inject
    RegistrationProperties registrationProperties;

    @Override
    public String requestEmailVerification(String email) {
        RegistrationAttempt attempt = registrationAttemptRepository.findByEmail(email)
                .orElseGet(() -> new RegistrationAttempt(sessionIdGenerator.generate(), email));

        String verificationCode = randomNumericCodeGenerator.generate(VERIFICATION_CODE_LENGTH);
        LocalDateTime verificationCodeExpirationTime = LocalDateTime.now().plusSeconds(
                registrationProperties.verificationCode().expirationSeconds());

        attempt.assignVerificationCode(verificationCode, verificationCodeExpirationTime);
        registrationAttemptRepository.save(attempt);

        return attempt.getId();
    }

    @Override
    public void verifyEmail(String sessionId, String verificationCode) {
        RegistrationAttempt attempt = registrationAttemptRepository.findById(sessionId)
                .orElseThrow(InvalidRegistrationSessionException::new);

        if (attempt.isEmailVerified()) {
            return; // Already verified, idempotent success
        }

        if (attempt.hasExceededMaxAttempts(registrationProperties.verificationCode().maxFailedVerificationAttempts())) {
            registrationAttemptRepository.delete(attempt);
            throw new VerificationCodeMaxAttemptsExceededException();
        }

        if (!attempt.isVerificationCodeCorrect(verificationCode)) {
            attempt.incrementFailedAttempts();
            registrationAttemptRepository.save(attempt);
            throw new InvalidVerificationCode();
        }

        attempt.markAsVerified();
        registrationAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public void completeRegistration(String sessionId, CompleteRegistrationRequest request) {
        RegistrationAttempt attempt = registrationAttemptRepository.findById(sessionId)
                .orElseThrow(InvalidRegistrationSessionException::new);

        if (!attempt.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (userRepository.existsByEmail(attempt.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(attempt.getEmail(), request.firstName(), request.lastName(), encodedPassword);
        user.verifyEmail(attempt.getEmailVerifiedAt());

        userRepository.save(user);
        registrationAttemptRepository.delete(attempt);
    }
}