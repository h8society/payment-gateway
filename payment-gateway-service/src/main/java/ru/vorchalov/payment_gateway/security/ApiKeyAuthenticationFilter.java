package ru.vorchalov.payment_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;

import java.io.IOException;
import java.util.Collections;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final MerchantKeyRepository keyRepository;

    public ApiKeyAuthenticationFilter(MerchantKeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isPublicPaymentGet = method.equals("GET") && path.matches("/api/payments/\\d+");

        if (isPublicPaymentGet) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-Api-Key");

        if (apiKey != null && path.startsWith("/api")) {
            MerchantKeyEntity key = keyRepository.findByApiKey(apiKey).orElse(null);

            if (key != null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        key.getUser().getUsername(),
                        null,
                        Collections.singleton(() -> "ROLE_MERCHANT")
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
