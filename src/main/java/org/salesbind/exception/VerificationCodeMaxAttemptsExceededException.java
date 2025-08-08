package org.salesbind.exception;

import jakarta.ws.rs.ForbiddenException;

public class VerificationCodeMaxAttemptsExceededException extends ForbiddenException {

    public VerificationCodeMaxAttemptsExceededException() {
        super("Maximum verification attempts exceeded.");
    }
}
