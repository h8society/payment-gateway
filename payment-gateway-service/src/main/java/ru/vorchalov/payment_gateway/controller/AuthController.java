package ru.vorchalov.payment_gateway.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.LoginRequest;
import ru.vorchalov.payment_gateway.dto.RegistrationRequest;
import ru.vorchalov.payment_gateway.security.RestAuthenticationSuccessHandler;
import ru.vorchalov.payment_gateway.service.auth.RegistrationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final RestAuthenticationSuccessHandler successHandler;
    private final AuthenticationManager authenticationManager;

    public AuthController(RegistrationService registrationService,
                          RestAuthenticationSuccessHandler successHandler,
                          AuthenticationManager authenticationManager) {
        this.registrationService = registrationService;
        this.successHandler = successHandler;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegistrationRequest request) {
        try {
            String apiKey = registrationService.registerMerchant(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Регистрация успешна");
            response.put("apiKey", apiKey);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ошибка при регистрации: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginRequest loginRequest,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException, ServletException {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword());

            Authentication authResult = authenticationManager.authenticate(authToken);

            successHandler.onAuthenticationSuccess(request, response, authResult);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка входа: " + e.getMessage());
        }
    }
}
