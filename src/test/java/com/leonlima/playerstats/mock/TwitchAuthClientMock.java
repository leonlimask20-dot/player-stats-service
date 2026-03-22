package com.leonlima.playerstats.mock;

import com.leonlima.playerstats.client.TwitchAuthClient;
import com.leonlima.playerstats.dto.PlayerDTO;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mock do TwitchAuthClient para os testes de integração.
 *
 * Sem esse mock, o Quarkus tenta inicializar o cliente REST da Twitch
 * durante o startup do @QuarkusTest, falha por não ter credenciais reais
 * e derruba todos os testes com erro 500.
 *
 * @Mock substitui o bean real apenas durante os testes — em produção
 * o cliente HTTP real é usado normalmente.
 */
@Mock
@ApplicationScoped
public class TwitchAuthClientMock implements TwitchAuthClient {

    @Override
    public PlayerDTO.TwitchTokenResponse getToken(
            String clientId,
            String clientSecret,
            String grantType) {
        // Retorna um token fictício — os testes não chamam o endpoint /sync
        // então esse token nunca é usado de verdade
        PlayerDTO.TwitchTokenResponse response = new PlayerDTO.TwitchTokenResponse();
        response.setAccessToken("mock-token-ci");
        response.setExpiresIn(3600L);
        return response;
    }
}
