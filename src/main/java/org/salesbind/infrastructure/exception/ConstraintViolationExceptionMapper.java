package org.salesbind.infrastructure.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<GlobalErrorResponse.FieldViolation> fieldViolations = exception.getConstraintViolations().stream()
                .map(v -> new GlobalErrorResponse.FieldViolation(extractFieldName(v.getPropertyPath()), v.getMessage()))
                .toList();

        GlobalErrorResponse response = new GlobalErrorResponse("An validation error occurred", fieldViolations);
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private String extractFieldName(Path path) {
        String fieldName = null;
        for (Path.Node node : path) {
            fieldName = node.getName();
        }

        return fieldName;
    }
}
