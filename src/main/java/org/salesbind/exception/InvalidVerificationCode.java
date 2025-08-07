package org.salesbind.exception;

import jakarta.ws.rs.BadRequestException;

public class InvalidVerificationCode extends BadRequestException {

    public InvalidVerificationCode() {
        super("Invalid verification code");
    }
}
