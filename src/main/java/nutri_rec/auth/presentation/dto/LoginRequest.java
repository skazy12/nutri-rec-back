package nutri_rec.auth.presentation.dto;

public record LoginRequest(
        String email,
        String password
) {}
