package nutri_rec.auth.application;

import nutri_rec.auth.domain.RefreshToken;
import nutri_rec.auth.infrastructure.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;


@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtProperties props;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repo, JwtProperties props) {
        this.repo = repo;
        this.props = props;
    }

    public String issueRefreshToken(UUID userId) {
        // Token random “alto”
        byte[] buf = new byte[64];
        random.nextBytes(buf);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(buf);

        var entity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(TokenHash.sha256(raw))
                .expiresAt(Instant.now().plusSeconds(props.refreshTtlDays() * 24 * 3600))
                .build();

        repo.save(entity);
        return raw;
    }

    public UUID validateRefreshTokenAndGetUser(String rawRefresh) {
        var hash = TokenHash.sha256(rawRefresh);
        var token = repo.findByTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (token.isRevoked()) throw new RuntimeException("Refresh token revocado");
        if (token.isExpired()) throw new RuntimeException("Refresh token expirado");

        return token.getUserId();
    }

    public void revokeRefreshToken(String rawRefresh) {
        var hash = TokenHash.sha256(rawRefresh);
        repo.findByTokenHash(hash).ifPresent(t -> {
            t.setRevokedAt(Instant.now());
            repo.save(t);
        });
    }
}
