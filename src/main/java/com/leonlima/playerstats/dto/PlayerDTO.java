package com.leonlima.playerstats.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leonlima.playerstats.model.Player;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class PlayerDTO {

    // --- Requisição ---

    @Data
    public static class SyncRequest {
        @NotBlank(message = "O login do streamer é obrigatório")
        private String login;
    }

    // --- Resposta ---

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String twitchId;
        private String displayName;
        private String login;
        private String profileImageUrl;
        private String description;
        private Long viewCount;
        private String broadcasterType;
        private LocalDateTime lastSyncAt;
        private LocalDateTime createdAt;

        public static Response fromEntity(Player player) {
            return Response.builder()
                    .id(player.id)
                    .twitchId(player.getTwitchId())
                    .displayName(player.getDisplayName())
                    .login(player.getLogin())
                    .profileImageUrl(player.getProfileImageUrl())
                    .description(player.getDescription())
                    .viewCount(player.getViewCount())
                    .broadcasterType(player.getBroadcasterType().name())
                    .lastSyncAt(player.getLastSyncAt())
                    .createdAt(player.getCreatedAt())
                    .build();
        }
    }

    // --- Twitch API: resposta de autenticação (OAuth2 client credentials) ---

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TwitchTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private Long expiresIn;
    }

    // --- Twitch API: envelope de resposta dos endpoints de usuário ---

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TwitchUsersResponse {
        private List<TwitchUserData> data;
    }

    // --- Twitch API: dados de um usuário/streamer ---

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TwitchUserData {
        private String id;

        @JsonProperty("display_name")
        private String displayName;

        private String login;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        private String description;

        @JsonProperty("view_count")
        private Long viewCount;

        @JsonProperty("broadcaster_type")
        private String broadcasterType;
    }
}
