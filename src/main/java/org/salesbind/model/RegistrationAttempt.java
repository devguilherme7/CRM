package org.salesbind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class RegistrationAttempt {

    private String id;
    private String email;
    private String verificationCode;
    private LocalDateTime codeExpiresAt;
    private LocalDateTime emailVerifiedAt;
    private int failedVerificationAttempts;

    public RegistrationAttempt(String id, String email) {
        this.id = id;
        this.email = email;
    }

    protected RegistrationAttempt() {
        // For jackson
    }

    public void assignVerificationCode(String verificationCode, LocalDateTime expiresAt) {
        this.verificationCode = verificationCode;
        this.codeExpiresAt = expiresAt;
        this.emailVerifiedAt = null; // Reset verification status on new code
        this.failedVerificationAttempts = 0; // Reset counter on new code
    }

    /**
     * Checks if the provided code is valid without changing the attempt's state.
     * This method is safe to call multiple times.
     */
    public boolean isVerificationCodeCorrect(String providedCode) {
        if (isVerificationCodeExpired() || verificationCode == null || providedCode == null) {
            return false;
        }
        // Use constant-time comparison to prevent timing attacks
        return MessageDigest.isEqual(verificationCode.getBytes(), providedCode.getBytes());
    }

    /**
     * Marks the email as verified and clears sensitive verification data.
     */
    public void markAsVerified() {
        this.emailVerifiedAt = LocalDateTime.now();
        this.verificationCode = null;
        this.codeExpiresAt = null;
        this.failedVerificationAttempts = 0;
    }

    public void incrementFailedAttempts() {
        this.failedVerificationAttempts++;
    }

    public boolean hasExceededMaxAttempts(int maxAttempts) {
        return this.failedVerificationAttempts >= maxAttempts;
    }

    @JsonIgnore
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    @JsonIgnore
    private boolean isVerificationCodeExpired() {
        return this.codeExpiresAt == null || this.codeExpiresAt.isBefore(LocalDateTime.now());
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

    public int getFailedVerificationAttempts() {
        return failedVerificationAttempts;
    }
}