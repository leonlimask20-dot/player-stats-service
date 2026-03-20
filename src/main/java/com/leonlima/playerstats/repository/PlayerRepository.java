package com.leonlima.playerstats.repository;

import com.leonlima.playerstats.model.Player;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositório que usa o padrão Repository do Panache.
 * Alternativa ao Active Record — separa a lógica de acesso a dados da entidade.
 * PanacheRepository já fornece findAll(), persist(), delete() e outros métodos prontos.
 */
@ApplicationScoped
public class PlayerRepository implements PanacheRepository<Player> {

    public Optional<Player> findByTwitchId(String twitchId) {
        return find("twitchId", twitchId).firstResultOptional();
    }

    public Optional<Player> findByLogin(String login) {
        return find("login", login).firstResultOptional();
    }

    // Busca por nome parcial, case-insensitive — útil para endpoint de pesquisa
    public List<Player> searchByName(String name) {
        return find("LOWER(displayName) LIKE ?1", "%" + name.toLowerCase() + "%").list();
    }

    public List<Player> findPartners() {
        return find("broadcasterType", Player.BroadcasterType.PARTNER).list();
    }
}
