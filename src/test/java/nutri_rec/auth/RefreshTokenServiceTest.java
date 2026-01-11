package nutri_rec.auth;

import nutri_rec.auth.application.JwtProperties;
import nutri_rec.auth.application.RefreshTokenService;
import nutri_rec.auth.application.TokenHash;
import nutri_rec.auth.domain.RefreshToken;
import nutri_rec.auth.infrastructure.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Test
    void test_issue_refresh_token_saves_hash_and_returns_raw() {
        System.out.println("[UT-BE-05] RefreshTokenService.issueRefreshToken guarda hash y devuelve raw");

        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        JwtProperties props = new JwtProperties("01234567890123456789012345678901", 60, 7);
        RefreshTokenService service = new RefreshTokenService(repo, props);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        UUID userId = UUID.randomUUID();
        String raw = service.issueRefreshToken(userId);

        verify(repo).save(captor.capture());
        RefreshToken saved = captor.getValue();

        String expectedHash = TokenHash.sha256(raw);

        System.out.println("  Entrada: userId=" + userId);
        System.out.println("  Obtenido:");
        System.out.println("    raw=" + raw);
        System.out.println("    tokenHashGuardado=" + saved.getTokenHash());
        System.out.println("    expiresAt=" + saved.getExpiresAt());
        System.out.println("  Esperado:");
        System.out.println("    tokenHashGuardado == sha256(raw)");
        System.out.println("    expiresAt > now");

        assertEquals(userId, saved.getUserId());
        assertEquals(expectedHash, saved.getTokenHash());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
        assertNotNull(raw);
        assertTrue(raw.length() > 20);
    }

    @Test
    void test_validate_refresh_token_ok() {
        System.out.println("[UT-BE-06] RefreshTokenService.validateRefreshTokenAndGetUser OK (Opción B)");

        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        JwtProperties props = new JwtProperties("01234567890123456789012345678901", 60, 7);
        RefreshTokenService service = new RefreshTokenService(repo, props);

        UUID userId = UUID.randomUUID();
        String raw = "RAW_DEMO";
        String hash = TokenHash.sha256(raw);

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revokedAt(null)
                .build();

        when(repo.findByTokenHash(hash)).thenReturn(Optional.of(token));

        UUID result = service.validateRefreshTokenAndGetUser(raw);

        System.out.println("  Entradas: raw=" + raw);
        System.out.println("  Obtenido: userId=" + result);
        System.out.println("  Esperado: userId == " + userId);

        assertEquals(userId, result);
    }

    @Test
    void test_revoke_refresh_token_marks_revokedAt_and_saves() {
        System.out.println("[UT-BE-07] RefreshTokenService.revokeRefreshToken marca revokedAt y guarda (Opción B)");

        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        JwtProperties props = new JwtProperties("01234567890123456789012345678901", 60, 7);
        RefreshTokenService service = new RefreshTokenService(repo, props);

        String raw = "RAW_REVOKE";
        String hash = TokenHash.sha256(raw);

        RefreshToken token = RefreshToken.builder()
                .userId(UUID.randomUUID())
                .tokenHash(hash)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revokedAt(null)
                .build();

        when(repo.findByTokenHash(hash)).thenReturn(Optional.of(token));

        service.revokeRefreshToken(raw);

        System.out.println("  Entrada: raw=" + raw);
        System.out.println("  Obtenido: revokedAt=" + token.getRevokedAt());
        System.out.println("  Esperado: revokedAt != null y repo.save llamado");

        assertNotNull(token.getRevokedAt());
        verify(repo).save(token);
    }
}
