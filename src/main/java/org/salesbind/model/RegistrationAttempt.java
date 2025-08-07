package org.salesbind.model;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class RegistrationAttempt {

    private String id;
    private String email;
    private String verificationCode;
    private LocalDateTime codeExpiresAt;

    public RegistrationAttempt(String id, String email) {
        this.id = id;
        this.email = email;
    }

    protected RegistrationAttempt() {
        //
    }

    public void assignVerificationCode(String verificationCode, LocalDateTime expiresAt) {
        this.verificationCode = verificationCode;
        this.codeExpiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public LocalDateTime getCodeExpiresAt() {
        return codeExpiresAt;
    }
}
