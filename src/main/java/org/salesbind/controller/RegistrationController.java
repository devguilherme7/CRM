package org.salesbind.controller;

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
import org.salesbind.dto.CompleteRegistrationRequest;
import org.salesbind.dto.VerifyEmailCodeRequest;
import org.salesbind.dto.RequestEmailVerificationRequest;
import org.salesbind.infrastructure.configuration.RegistrationProperties;
import org.salesbind.service.RegistrationService;

@Path("/v1/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationProperties registrationProperties;

    public RegistrationController(RegistrationService registrationService,
            RegistrationProperties registrationProperties) {
        this.registrationService = registrationService;
        this.registrationProperties = registrationProperties;
    }

    @POST
    @Path("/requestEmailVerification")
    public Response requestEmailVerification(@Valid RequestEmailVerificationRequest request) {
        String sessionId = registrationService.requestEmailVerification(request.email());
        NewCookie sessionCookie = new NewCookie.Builder("_UAC_SID")
                .value(sessionId).path("/v1/registrations").maxAge(registrationProperties.session().expirationSeconds())
                .secure(true).httpOnly(true).sameSite(NewCookie.SameSite.STRICT).build();

        return Response.accepted().cookie(sessionCookie).build();
    }

    @POST
    @Path("/verifyEmail")
    public Response verifyEmail(@CookieParam("_UAC_SID") String sessionId, @Valid VerifyEmailCodeRequest request) {
        if (sessionId == null) {
            throw new BadRequestException("Registration session not found");
        }

        registrationService.verifyEmail(sessionId, request.verificationCode());
        return Response.noContent().build();
    }

    @POST
    @Path("/completeRegistration")
    public Response completeRegistration(@CookieParam("_UAC_SID") String sessionId,
            @Valid CompleteRegistrationRequest request) {

        if (sessionId == null) {
            throw new BadRequestException("Registration session not found");
        }

        registrationService.completeRegistration(sessionId, request);

        NewCookie clearCookie = new NewCookie.Builder("_UAC_SID")
                .value("").path("/").httpOnly(true).maxAge(0).build();

        return Response.status(Response.Status.CREATED).cookie(clearCookie).build();
    }
}
