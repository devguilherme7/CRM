package org.salesbind.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.salesbind.dto.CompleteRegistrationRequest;
import org.salesbind.dto.RequestEmailVerificationRequest;
import org.salesbind.dto.VerifyEmailCodeRequest;
import org.salesbind.infrastructure.configuration.RegistrationProperties;
import org.salesbind.service.RegistrationService;

@Path("/v1/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Registration Flow", description = "Endpoints for the multi-step user registration process.")
public class RegistrationController {

    @Inject
    RegistrationService registrationService;

    @Inject
    RegistrationProperties registrationProperties;

    @POST
    @Path("/requestEmailVerification")
    @Operation(
            summary = "Request Email Verification",
            description = "Starts the registration process by sending a verification code to the user's email. " +
                    "This endpoint is idempotent.")
    @APIResponse(
            responseCode = "202",
            description = "A verification code has been sent to the email. A `_UAC_SID` session cookie is set " +
                    "in the response to track the registration flow.")
    @APIResponse(responseCode = "400", description = "The request body is invalid (e.g., malformed email).")
    public Response requestEmailVerification(@Valid RequestEmailVerificationRequest request) {
        String sessionId = registrationService.requestEmailVerification(request.email());
        NewCookie sessionCookie = new NewCookie.Builder("_UAC_SID").value(sessionId)
                .path("/v1/registrations").maxAge(registrationProperties.session().expirationSeconds())
                .secure(true).httpOnly(true).sameSite(NewCookie.SameSite.STRICT).build();

        return Response.accepted().cookie(sessionCookie).build();
    }

    @POST
    @Path("/verifyEmail")
    @Operation(summary = "Verify Email", description = "Verifies the email address using the code sent to the user.")
    @Parameter(name = "_UAC_SID", in = ParameterIn.COOKIE, required = true,
            description = "The session ID cookie obtained from the first step.")
    @APIResponse(
            responseCode = "204",
            description = "Email successfully verified. The session is now authorized to complete the registration.")
    @APIResponse(responseCode = "400", description = "The session is invalid or the verification code is incorrect.")
    @APIResponse(responseCode = "403", description = "Maximum verification attempts have been exceeded and the " +
            "session has been terminated.")
    public Response verifyEmail(@CookieParam("_UAC_SID") String sessionId, @Valid VerifyEmailCodeRequest request) {
        if (sessionId == null) {
            throw new BadRequestException("Registration session not found");
        }

        registrationService.verifyEmail(sessionId, request.verificationCode());
        return Response.noContent().build();
    }

    @POST
    @Path("/completeRegistration")
    @Operation(summary = "Step 3: Complete Registration",
            description = "Finalizes the registration by creating the user account with the provided details.")
    @Parameter(name = "_UAC_SID", in = ParameterIn.COOKIE, required = true,
            description = "The session ID cookie from the verified session.")
    @APIResponse(responseCode = "201", description = "User account successfully created.")
    @APIResponse(responseCode = "400", description = "The session is invalid, the email has not been verified, " +
            "or the request data is invalid (e.g., weak password).")
    @APIResponse(responseCode = "409", description = "A user with this email address already exists.")
    public Response completeRegistration(@CookieParam("_UAC_SID") String sessionId,
            @Valid CompleteRegistrationRequest request) {

        if (sessionId == null) {
            throw new BadRequestException("Registration session not found");
        }

        registrationService.completeRegistration(sessionId, request);

        // Clear the session cookie upon successful registration
        NewCookie clearCookie = new NewCookie.Builder("_UAC_SID").value("").path("/v1/registrations")
                .httpOnly(true).secure(true).maxAge(0).build();

        return Response.status(Response.Status.CREATED).cookie(clearCookie).build();
    }
}