package org.salesbind.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class EmailAlreadyExistsException extends WebApplicationException {

    public EmailAlreadyExistsException() {
        super("Email already exists", Response.Status.CONFLICT);
    }
}
