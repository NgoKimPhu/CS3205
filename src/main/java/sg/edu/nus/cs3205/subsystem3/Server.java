package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import sg.edu.nus.cs3205.subsystem3.api.oauth.TokenGranter;
import sg.edu.nus.cs3205.subsystem3.api.session.Session;
import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.objects.GrantClaim;
import sg.edu.nus.cs3205.subsystem3.objects.Links;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Server {
    @Context
    UriInfo uri;

    @GET
    public Response getRoot() {
        return Response.ok(new Links(Links.newLink(this.uri, "", "self", "GET"),
                Links.newLink(this.uri, "oauth/token", "oauth.token", "POST"),
                Links.newLink(this.uri, "upload", "upload", "POST"))).build();
    }

    @Path("/oauth/token")
    public TokenGranter grant() {
        return new TokenGranter();
    }

    @Path("/session")
    public Session session(@HeaderParam("Authorization") final String accessToken,
            @HeaderParam("X-NFC-Token") final String nfcToken) {
        GrantClaim claim;
        if (accessToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing Authorization header");
        } else if (!accessToken.startsWith("Bearer ")) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Bearer access token required");
        }
        try {
            claim = TokenUtils.verifyJWT(accessToken.substring("Bearer ".length()));
        } catch (final Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, e);
        }

        if (nfcToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing X-NFC-Token header");
        }
        try {
            nfcToken.charAt(0);
        } catch (final Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Invalid NFC token");
        }

        return new Session(this.uri, claim.userId);
    }

    @Deprecated
    @Path("/upload")
    public Session upload(@HeaderParam("Authorization") final String accessToken,
            @HeaderParam("X-NFC-Token") final String nfcToken) {
        return this.session(accessToken, nfcToken);
    }
}
