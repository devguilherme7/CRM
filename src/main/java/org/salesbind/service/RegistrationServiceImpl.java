package org.salesbind.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.salesbind.dto.CompleteRegistrationRequest;
import org.salesbind.exception.EmailAlreadyExistsException;
import org.salesbind.exception.EmailNotVerifiedException;
import org.salesbind.exception.InvalidRegistrationSessionException;
import org.salesbind.exception.InvalidVerificationCode;
import org.salesbind.infrastructure.security.PasswordEncoder;
import org.salesbind.infrastructure.security.RandomNumericCodeGenerator;
import org.salesbind.infrastructure.session.SessionIdGenerator;
import org.salesbind.model.RegistrationAttempt;
import org.salesbind.model.User;
import org.salesbind.repository.RegistrationAttemptRepository;
import org.salesbind.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegistrationServiceImpl implements RegistrationService {

    public static final int VERIFICATION_CODE_LENGTH = 6;

    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final SessionIdGenerator sessionIdGenerator;
    private final RandomNumericCodeGenerator randomNumericCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public RegistrationServiceImpl(RegistrationAttemptRepository registrationAttemptRepository,
            SessionIdGenerator sessionIdGenerator, RandomNumericCodeGenerator randomNumericCodeGenerator,
            PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.registrationAttemptRepository = registrationAttemptRepository;
        this.sessionIdGenerator = sessionIdGenerator;
        this.randomNumericCodeGenerator = randomNumericCodeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public String requestEmailVerification(String email) {
        RegistrationAttempt attempt = registrationAttemptRepository.findByEmail(email)
                .orElseGet(() -> new RegistrationAttempt(sessionIdGenerator.generate(), email));

        String verificationCode = randomNumericCodeGenerator.generate(VERIFICATION_CODE_LENGTH);
        LocalDateTime verificationCodeExpirationTime = LocalDateTime.now().plus(Duration.ofMinutes(15));

        attempt.assignVerificationCode(verificationCode, verificationCodeExpirationTime);
        registrationAttemptRepository.save(attempt);

        return attempt.getId();
    }

    @Override
    public void verifyEmail(String sessionId, String verificationCode) {
        RegistrationAttempt attempt = registrationAttemptRepository.findById(sessionId)
                .orElseThrow(InvalidRegistrationSessionException::new);

        boolean verified = attempt.verifyEmail(verificationCode);
        if (!verified) {
            throw new InvalidVerificationCode();
        }

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
