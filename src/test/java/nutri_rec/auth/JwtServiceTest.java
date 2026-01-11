package nutri_rec.auth;

import nutri_rec.auth.application.JwtProperties;
import nutri_rec.auth.application.JwtService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService buildService() {
        // IMPORTANTE: HS256 requiere secret >= 32 bytes
        String secret = "01234567890123456789012345678901";
        JwtProperties props = new JwtProperties(secret, 60, 7);
        return new JwtService(props);
    }

    @Test
    void test_generate_and_validate_access_token_ok() {
        System.out.println("[UT-BE-01] JwtService generate + validate");
        JwtService jwt = buildService();

        UUID userId = UUID.randomUUID();
        String email = "omar@test.com";

        String token = jwt.generateAccessToken(userId, email);
        UUID parsed = jwt.validateAndGetUserId(token);

        System.out.println("  Entradas:");
        System.out.println("    userId=" + userId);
        System.out.println("    email=" + email);
        System.out.println("  Obtenido:");
        System.out.println("    token=" + token);
        System.out.println("    validateAndGetUserId(token)=" + parsed);
        System.out.println("  Esperado: parsed == userId");

        assertEquals(userId, parsed);
    }

    @Test
    void test_validate_invalid_token_throws() {
        System.out.println("[UT-BE-02] JwtService token inválido lanza excepción (Opción B)");
        JwtService jwt = buildService();

        String invalid = "token.invalido.fake";

        System.out.println("  Entrada: token=" + invalid);
        System.out.println("  Esperado: Exception al validar");

        assertThrows(Exception.class, () -> jwt.validateAndGetUserId(invalid));
    }
}
