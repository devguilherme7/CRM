package org.salesbind.exception;

import jakarta.ws.rs.BadRequestException;

public class InvalidRegistrationSessionException extends BadRequestException {

    public InvalidRegistrationSessionException() {
        super("Invalid session");
    }
}
