package org.salesbind.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import org.salesbind.infrastructure.security.RandomNumericCodeGenerator;
import org.salesbind.infrastructure.session.SessionIdGenerator;
import org.salesbind.model.RegistrationAttempt;
import org.salesbind.repository.RegistrationAttemptRepository;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegistrationServiceImpl implements RegistrationService {

    public static final int VERIFICATION_CODE_LENGTH = 6;

    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final SessionIdGenerator sessionIdGenerator;
    private final RandomNumericCodeGenerator randomNumericCodeGenerator;

    public RegistrationServiceImpl(RegistrationAttemptRepository registrationAttemptRepository,
            SessionIdGenerator sessionIdGenerator, RandomNumericCodeGenerator randomNumericCodeGenerator) {
        this.registrationAttemptRepository = registrationAttemptRepository;
        this.sessionIdGenerator = sessionIdGenerator;
        this.randomNumericCodeGenerator = randomNumericCodeGenerator;
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
                .orElseThrow(() -> new BadRequestException("Invalid session"));

        boolean verified = attempt.verifyEmail(verificationCode);
        if (verified) {
            throw new BadRequestException("Invalid verification code");
        }

        registrationAttemptRepository.save(attempt);
    }
}
