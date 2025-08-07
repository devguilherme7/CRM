package org.salesbind.service;

import org.salesbind.dto.CompleteRegistrationRequest;

public interface RegistrationService {

    String requestEmailVerification(String email);

    void verifyEmail(String sessionId, String verificationCode);

    void completeRegistration(String sessionId, CompleteRegistrationRequest request);
}
