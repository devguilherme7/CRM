package org.salesbind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestEmailVerificationRequest(@NotBlank @Email String email) {

}
