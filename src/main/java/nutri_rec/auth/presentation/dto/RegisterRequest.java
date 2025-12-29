package nutri_rec.auth.presentation.dto;

public record RegisterRequest(
        String email,
        String password
) {}
