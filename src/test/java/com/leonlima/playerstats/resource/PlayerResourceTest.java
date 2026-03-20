package com.leonlima.playerstats.resource;

import com.leonlima.playerstats.model.Player;
import com.leonlima.playerstats.repository.PlayerRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração com @QuarkusTest.
 *
 * @QuarkusTest sobe a aplicação completa em modo de teste — incluindo banco H2,
 * CDI e o servidor HTTP na porta 8083. O RestAssured faz requisições HTTP reais
 * contra a aplicação, testando o fluxo de ponta a ponta sem mocks.
 *
 * Diferença em relação aos testes unitários com Mockito:
 * aqui testamos a integração entre Resource, Service e Repository.
 */
@QuarkusTest
@DisplayName("PlayerResource — testes de integração")
class PlayerResourceTest {

    @Inject
    PlayerRepository playerRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpa o banco antes de cada teste para garantir isolamento
        playerRepository.deleteAll();

        Player player = new Player();
        player.setTwitchId("123456");
        player.setDisplayName("PokerStreamer BR");
        player.setLogin("pokerstreamer_br");
        player.setProfileImageUrl("https://example.com/img.jpg");
        player.setDescription("Poker ao vivo todo dia");
        player.setViewCount(150000L);
        player.setBroadcasterType(Player.BroadcasterType.PARTNER);
        player.setLastSyncAt(LocalDateTime.now());
        playerRepository.persist(player);
    }

    @Test
    @DisplayName("GET /api/players deve retornar lista de jogadores")
    void findAll_returnsPlayerList() {
        given()
            .when().get("/api/players")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].displayName", notNullValue());
    }

    @Test
    @DisplayName("GET /api/players/{id} deve retornar jogador existente")
    void findById_existingPlayer_returnsPlayer() {
        Long id = playerRepository.findByLogin("pokerstreamer_br").get().id;

        given()
            .pathParam("id", id)
            .when().get("/api/players/{id}")
            .then()
                .statusCode(200)
                .body("login", equalTo("pokerstreamer_br"))
                .body("displayName", equalTo("PokerStreamer BR"))
                .body("broadcasterType", equalTo("PARTNER"));
    }

    @Test
    @DisplayName("GET /api/players/{id} deve retornar 404 para ID inexistente")
    void findById_nonExistent_returns404() {
        given()
            .pathParam("id", 99999L)
            .when().get("/api/players/{id}")
            .then()
                .statusCode(404)
                .body("message", containsString("99999"));
    }

    @Test
    @DisplayName("GET /api/players/login/{login} deve retornar jogador pelo login")
    void findByLogin_existingLogin_returnsPlayer() {
        given()
            .pathParam("login", "pokerstreamer_br")
            .when().get("/api/players/login/{login}")
            .then()
                .statusCode(200)
                .body("twitchId", equalTo("123456"));
    }

    @Test
    @DisplayName("GET /api/players/search deve filtrar por nome")
    void search_byName_returnsMatchingPlayers() {
        given()
            .queryParam("name", "poker")
            .when().get("/api/players/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/players/partners deve retornar apenas parceiros")
    void findPartners_returnsOnlyPartners() {
        given()
            .when().get("/api/players/partners")
            .then()
                .statusCode(200)
                .body("$", everyItem(hasEntry("broadcasterType", "PARTNER")));
    }

    @Test
    @DisplayName("POST /api/players/sync sem body deve retornar 400")
    void syncPlayer_missingBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/players/sync")
            .then()
                .statusCode(400);
    }
}
