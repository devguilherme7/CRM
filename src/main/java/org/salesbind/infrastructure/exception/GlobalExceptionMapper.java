package org.salesbind.infrastructure.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException wae) {
            GlobalErrorResponse response = new GlobalErrorResponse(wae.getMessage());
            return Response.status(wae.getResponse().getStatus()).entity(response).build();
        }

        GlobalErrorResponse response = new GlobalErrorResponse("Internal server error occurred");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }
}
