package com.leonlima.playerstats.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * No Quarkus/JAX-RS o tratamento de exceções é feito via ExceptionMapper.
 * @Provider registra o mapper automaticamente no container CDI.
 *
 * Nota: o mapper genérico de Exception foi removido intencionalmente —
 * ele interceptava requisições internas do Quarkus (Swagger UI, health check)
 * causando erro 500 nessas rotas.
 */
@Slf4j
public class GlobalExceptionMapper {

    @Provider
    public static class NotFoundMapper implements ExceptionMapper<PlayerNotFoundException> {
        @Override
        public Response toResponse(PlayerNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, ex.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class TwitchIntegrationMapper implements ExceptionMapper<TwitchIntegrationException> {
        @Override
        public Response toResponse(TwitchIntegrationException ex) {
            log.error("Erro de integração com a Twitch API: {}", ex.getMessage());
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(errorBody(502, ex.getMessage()))
                    .build();
        }
    }

    private static Map<String, Object> errorBody(int status, String message) {
        return Map.of(
                "status", status,
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
