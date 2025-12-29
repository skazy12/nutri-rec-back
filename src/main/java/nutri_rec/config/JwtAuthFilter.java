package nutri_rec.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nutri_rec.auth.application.JwtService;
import nutri_rec.user.infrastructure.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var userId = jwtService.validateAndGetUserId(token);
                var user = userRepo.findById(userId).orElse(null);

                if (user != null) {
                    var authentication = SecurityConfig.buildAuth(user.getEmail());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Guardamos el userId en request para usarlo fácil en controllers (opcional)
                    request.setAttribute("userId", userId);
                }
            } catch (Exception ignored) {
                // Token inválido: no autenticamos, y caerá por "authenticated()" si es endpoint protegido
            }
        }

        chain.doFilter(request, response);
    }
}
