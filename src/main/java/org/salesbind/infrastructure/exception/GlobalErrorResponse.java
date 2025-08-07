package org.salesbind.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GlobalErrorResponse(String error, List<FieldViolation> fieldViolations) {

    public GlobalErrorResponse(String error) {
        this(error, null);
    }

    public record FieldViolation(String field, String error) {}
}
