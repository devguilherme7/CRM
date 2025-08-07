package org.salesbind.model;

import java.security.MessageDigest;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class RegistrationAttempt {

    private String id;
    private String email;
    private String verificationCode;
    private LocalDateTime codeExpiresAt;
    private LocalDateTime emailVerifiedAt;

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

    public boolean verifyEmail(String providedCode) {
        if (emailVerifiedAt != null) {
            return true;
        }

        if (isVerificationCodeExpired() || !isVerificationCodeMatching(providedCode)) {
            return false;
        }

        markAsVerified();
        return true;
    }

    private boolean isVerificationCodeExpired() {
        return this.codeExpiresAt.isBefore(LocalDateTime.now());
    }

    private boolean isVerificationCodeMatching(String providedCode) {
        return MessageDigest.isEqual(verificationCode.getBytes(), providedCode.getBytes());
    }

    private void markAsVerified() {
        this.emailVerifiedAt = LocalDateTime.now();
        this.verificationCode = null;
        this.codeExpiresAt = null;
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

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }
}
