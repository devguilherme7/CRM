package org.salesbind.service;

public interface RegistrationService {

    String requestEmailVerification(String email);

    void verifyEmail(String sessionId, String verificationCode);
}
