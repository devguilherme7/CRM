package org.salesbind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequest(@NotBlank @Pattern(regexp = "^\\d{6}$") String verificationCode) {

}
