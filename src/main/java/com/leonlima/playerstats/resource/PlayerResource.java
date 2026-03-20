package com.leonlima.playerstats.resource;

import com.leonlima.playerstats.dto.PlayerDTO;
import com.leonlima.playerstats.service.PlayerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * No Quarkus/JAX-RS os endpoints são definidos com anotações JAX-RS (@GET, @POST, @Path),
 * diferente do @GetMapping/@PostMapping do Spring MVC.
 * @Path define o prefixo de todas as rotas deste resource.
 */
@Path("/api/players")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Players", description = "Consulta e sincronização de streamers de poker via Twitch API")
@Slf4j
public class PlayerResource {

    @Inject
    PlayerService playerService;

    @GET
    @Operation(summary = "Listar todos os jogadores sincronizados")
    public List<PlayerDTO.Response> findAll() {
        return playerService.findAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar jogador por ID local")
    public PlayerDTO.Response findById(@PathParam("id") Long id) {
        return playerService.findById(id);
    }

    @GET
    @Path("/login/{login}")
    @Operation(summary = "Buscar jogador pelo login da Twitch")
    public PlayerDTO.Response findByLogin(@PathParam("login") String login) {
        return playerService.findByLogin(login);
    }

    // Parâmetro de query: GET /api/players/search?name=poker
    @GET
    @Path("/search")
    @Operation(summary = "Buscar jogadores por nome (parcial, case-insensitive)")
    public List<PlayerDTO.Response> search(@QueryParam("name") String name) {
        return playerService.searchByName(name);
    }

    @GET
    @Path("/partners")
    @Operation(summary = "Listar apenas streamers parceiros da Twitch")
    public List<PlayerDTO.Response> findPartners() {
        return playerService.findPartners();
    }

    /**
     * Sincroniza os dados de um streamer com a Twitch API.
     * Comportamento de upsert: atualiza se já existe, insere se for novo.
     * Idempotente: chamadas repetidas atualizam o registro sem duplicar.
     */
    @POST
    @Path("/sync")
    @Operation(
        summary = "Sincronizar streamer com a Twitch API",
        description = "Busca dados atualizados na Twitch e faz upsert no banco local. " +
                      "Idempotente: chamadas repetidas atualizam o registro existente sem duplicar."
    )
    public Response syncPlayer(@Valid PlayerDTO.SyncRequest request) {
        log.info("Requisição de sync recebida para login={}", request.getLogin());
        PlayerDTO.Response response = playerService.syncPlayer(request.getLogin());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
