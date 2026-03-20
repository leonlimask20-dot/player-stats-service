package com.leonlima.playerstats.service;

import com.leonlima.playerstats.client.TwitchAuthClient;
import com.leonlima.playerstats.client.TwitchClient;
import com.leonlima.playerstats.dto.PlayerDTO;
import com.leonlima.playerstats.exception.PlayerNotFoundException;
import com.leonlima.playerstats.exception.TwitchIntegrationException;
import com.leonlima.playerstats.model.Player;
import com.leonlima.playerstats.repository.PlayerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
@Slf4j
public class PlayerService {

    @Inject
    PlayerRepository playerRepository;

    // @RestClient é a anotação do MicroProfile para injetar o proxy gerado
    @Inject
    @RestClient
    TwitchClient twitchClient;

    @Inject
    @RestClient
    TwitchAuthClient twitchAuthClient;

    @ConfigProperty(name = "twitch.client-id")
    String clientId;

    @ConfigProperty(name = "twitch.client-secret")
    String clientSecret;

    // Cache simples do token em memória para evitar chamadas desnecessárias ao endpoint de auth
    private String cachedToken;
    private LocalDateTime tokenExpiresAt;

    public List<PlayerDTO.Response> findAll() {
        return playerRepository.findAll().list().stream()
                .map(PlayerDTO.Response::fromEntity)
                .toList();
    }

    public PlayerDTO.Response findById(Long id) {
        return playerRepository.findByIdOptional(id)
                .map(PlayerDTO.Response::fromEntity)
                .orElseThrow(() -> new PlayerNotFoundException("Jogador não encontrado com id: " + id));
    }

    public PlayerDTO.Response findByLogin(String login) {
        return playerRepository.findByLogin(login)
                .map(PlayerDTO.Response::fromEntity)
                .orElseThrow(() -> new PlayerNotFoundException("Jogador não encontrado: " + login));
    }

    public List<PlayerDTO.Response> searchByName(String name) {
        return playerRepository.searchByName(name).stream()
                .map(PlayerDTO.Response::fromEntity)
                .toList();
    }

    public List<PlayerDTO.Response> findPartners() {
        return playerRepository.findPartners().stream()
                .map(PlayerDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Busca os dados do streamer na Twitch API e sincroniza com o banco local.
     * Se o jogador já existir (mesmo twitchId), atualiza os dados. Caso contrário, insere.
     */
    @Transactional
    public PlayerDTO.Response syncPlayer(String login) {
        log.info("Sincronizando jogador: {}", login);

        String token = getValidToken();

        PlayerDTO.TwitchUsersResponse response;
        try {
            response = twitchClient.getUsersByLogin("Bearer " + token, clientId, login);
        } catch (Exception e) {
            log.error("Falha ao consultar Twitch API para login={}: {}", login, e.getMessage());
            throw new TwitchIntegrationException("Falha ao consultar a Twitch API: " + e.getMessage());
        }

        if (response.getData() == null || response.getData().isEmpty()) {
            throw new PlayerNotFoundException("Streamer não encontrado na Twitch: " + login);
        }

        PlayerDTO.TwitchUserData userData = response.getData().get(0);

        // Upsert: atualiza se já existe, insere se for novo
        Player player = playerRepository.findByTwitchId(userData.getId())
                .orElse(new Player());

        player.setTwitchId(userData.getId());
        player.setDisplayName(userData.getDisplayName());
        player.setLogin(userData.getLogin());
        player.setProfileImageUrl(userData.getProfileImageUrl());
        player.setDescription(userData.getDescription());
        player.setViewCount(userData.getViewCount());
        player.setBroadcasterType(parseBroadcasterType(userData.getBroadcasterType()));

        playerRepository.persist(player);

        log.info("Jogador {} sincronizado com sucesso — id local={}", login, player.id);
        return PlayerDTO.Response.fromEntity(player);
    }

    /**
     * Retorna o token em cache se ainda válido, ou solicita um novo.
     * A Twitch retorna tokens com validade de ~60 dias no client credentials flow.
     */
    private String getValidToken() {
        if (cachedToken != null && tokenExpiresAt != null && LocalDateTime.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        log.debug("Token expirado ou ausente — solicitando novo token à Twitch");

        PlayerDTO.TwitchTokenResponse tokenResponse = twitchAuthClient.getToken(
                clientId, clientSecret, "client_credentials"
        );

        cachedToken = tokenResponse.getAccessToken();
        // Subtrai 60 segundos da expiração como margem de segurança
        tokenExpiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 60);

        return cachedToken;
    }

    private Player.BroadcasterType parseBroadcasterType(String type) {
        if (type == null || type.isBlank()) return Player.BroadcasterType.NONE;
        return switch (type.toLowerCase()) {
            case "partner" -> Player.BroadcasterType.PARTNER;
            case "affiliate" -> Player.BroadcasterType.AFFILIATE;
            default -> Player.BroadcasterType.NONE;
        };
    }
}
