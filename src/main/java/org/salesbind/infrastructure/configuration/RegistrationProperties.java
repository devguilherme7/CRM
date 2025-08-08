package org.salesbind.infrastructure.configuration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.registration")
public interface RegistrationProperties {

    Session session();

    VerificationCode verificationCode();

    interface Session {

        int expirationSeconds();
    }

    interface VerificationCode {

        int expirationSeconds();

        int maxFailedVerificationAttempts();
    }
}
