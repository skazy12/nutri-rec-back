package nutri_rec.auth.presentation;

import nutri_rec.auth.application.JwtService;
import nutri_rec.auth.application.RefreshTokenService;
import nutri_rec.auth.presentation.dto.*;
import nutri_rec.user.domain.User;
import nutri_rec.user.domain.UserProfile;
import nutri_rec.user.infrastructure.UserProfileRepository;
import nutri_rec.user.infrastructure.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;

    public AuthController(UserRepository userRepo,
                          UserProfileRepository profileRepo,
                          PasswordEncoder encoder,
                          JwtService jwtService,
                          RefreshTokenService refreshService) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshService = refreshService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest body) {
        if (body.email() == null || body.password() == null) {
            return ResponseEntity.badRequest().body("email y password son obligatorios");
        }
        if (userRepo.existsByEmailIgnoreCase(body.email())) {
            return ResponseEntity.badRequest().body("Ese email ya está registrado");
        }

        UUID userId = UUID.randomUUID();

        var user = User.builder()
                .id(userId)
                .email(body.email().trim().toLowerCase())
                .passwordHash(encoder.encode(body.password()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepo.save(user);

        // Creamos perfil vacío para que luego lo edite
        var profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .almuerzoCenaMisma(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        profileRepo.save(profile);

        // Tokens
        var access = jwtService.generateAccessToken(userId, user.getEmail());
        var refresh = refreshService.issueRefreshToken(userId);

        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        var userOpt = userRepo.findByEmailIgnoreCase(body.email() == null ? "" : body.email().trim());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Credenciales inválidas");

        var user = userOpt.get();
        if (!encoder.matches(body.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        var access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        var refresh = refreshService.issueRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest body) {
        try {
            var userId = refreshService.validateRefreshTokenAndGetUser(body.refreshToken());
            var user = userRepo.findById(userId).orElseThrow();

            // Generamos un nuevo access token (y dejamos refresh igual; luego podemos rotarlo si quieres)
            var access = jwtService.generateAccessToken(userId, user.getEmail());
            return ResponseEntity.ok(new AuthResponse(access, body.refreshToken()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Refresh inválido: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest body) {
        refreshService.revokeRefreshToken(body.refreshToken());
        return ResponseEntity.ok("logout_ok");
    }
}
