package com.leonlima.playerstats.client;

import com.leonlima.playerstats.dto.PlayerDTO;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente para o endpoint de autenticação da Twitch (OAuth2 client credentials).
 * Separado do TwitchClient pois usa uma URL base diferente (id.twitch.tv vs api.twitch.tv).
 */
@RegisterRestClient(configKey = "twitch-auth")
@Path("/oauth2")
@Produces(MediaType.APPLICATION_JSON)
public interface TwitchAuthClient {

    /**
     * Obtém um token de acesso via OAuth2 client credentials flow.
     * O token retornado é usado como "Bearer <token>" nas chamadas à Twitch API.
     */
    @POST
    @Path("/token")
    PlayerDTO.TwitchTokenResponse getToken(
            @QueryParam("client_id") String clientId,
            @QueryParam("client_secret") String clientSecret,
            @QueryParam("grant_type") String grantType
    );
}
