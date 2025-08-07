package org.salesbind.exception;

import jakarta.ws.rs.BadRequestException;

public class EmailNotVerifiedException extends BadRequestException {

    public EmailNotVerifiedException() {
        super("Check email first");
    }
}
