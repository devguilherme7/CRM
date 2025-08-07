package org.salesbind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailCodeRequest(@NotBlank @Pattern(regexp = "^\\d{6}$") String verificationCode) {

}
