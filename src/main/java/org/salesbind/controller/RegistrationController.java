package org.salesbind.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.salesbind.dto.EmailVerificationRequest;
import org.salesbind.dto.RequestEmailVerificationRequest;
import org.salesbind.service.RegistrationService;

@Path("/v1/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @POST
    @Path("/requestEmailVerification")
    public Response requestEmailVerification(@Valid RequestEmailVerificationRequest request) {
        String sessionId = registrationService.requestEmailVerification(request.email());
        NewCookie sessionCookie = new NewCookie.Builder("_UAC_SID")
                .value(sessionId).path("/v1/registrations").maxAge(3600).secure(true).httpOnly(true)
                .sameSite(NewCookie.SameSite.STRICT).build();

        return Response.accepted().cookie(sessionCookie).build();
    }

    @POST
    @Path("/verifyEmail")
    public Response verifyEmail(@CookieParam("_UAC_SID") String sessionId, @Valid EmailVerificationRequest request) {
        registrationService.verifyEmail(sessionId, request.verificationCode());
        return Response.noContent().build();
    }
}
