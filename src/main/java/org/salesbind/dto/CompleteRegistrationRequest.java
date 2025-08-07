package org.salesbind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationRequest(
        @NotBlank @Size(min = 3, max = 50) String firstName,
        @NotBlank @Size(min = 3, max = 50) String lastName,
        @NotBlank String password) {

}
