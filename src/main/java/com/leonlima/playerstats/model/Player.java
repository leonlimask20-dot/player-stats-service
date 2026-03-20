package com.leonlima.playerstats.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que usa o padrão Active Record do Panache.
 * Ao estender PanacheEntity, a classe herda automaticamente o campo "id" (Long)
 * e métodos estáticos como Player.findAll(), Player.findById(), Player.persist(), etc.
 * Elimina a necessidade de um repositório separado para operações básicas.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player extends PanacheEntity {

    @Column(nullable = false, unique = true)
    private String twitchId;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String login;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Número de seguidores obtido da Twitch API na última sincronização
    private Long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BroadcasterType broadcasterType;

    // Registro do momento da última sincronização com a Twitch API
    @Column(nullable = false)
    private LocalDateTime lastSyncAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastSyncAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastSyncAt = LocalDateTime.now();
    }

    public enum BroadcasterType {
        PARTNER,   // streamer parceiro oficial da Twitch
        AFFILIATE, // streamer afiliado
        NONE       // canal comum
    }
}
