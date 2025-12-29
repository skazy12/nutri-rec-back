package nutri_rec.auth.presentation.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
