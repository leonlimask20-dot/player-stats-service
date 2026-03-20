package com.leonlima.playerstats.client;

import com.leonlima.playerstats.dto.PlayerDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente declarativo para a Twitch API via MicroProfile REST Client.
 *
 * @RegisterRestClient: o Quarkus gera a implementação em tempo de build.
 * A URL base é configurada em application.properties via:
 *   quarkus.rest-client.twitch-api.url=https://api.twitch.tv
 *
 * Não é necessário implementar esta interface manualmente —
 * o framework injeta o proxy gerado onde TwitchClient for usado.
 */
@RegisterRestClient(configKey = "twitch-api")
@Path("/helix")
@Produces(MediaType.APPLICATION_JSON)
public interface TwitchClient {

    /**
     * Busca dados de um ou mais usuários pelo login.
     * O header Authorization usa o token OAuth2 obtido previamente.
     * O header Client-Id é obrigatório pela Twitch API em todos os endpoints.
     */
    @GET
    @Path("/users")
    PlayerDTO.TwitchUsersResponse getUsersByLogin(
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("Client-Id") String clientId,
            @QueryParam("login") String login
    );
}
