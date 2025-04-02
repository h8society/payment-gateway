package ru.vorchalov.payment_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vorchalov.payment_gateway.dto.RegistrationRequest;
import ru.vorchalov.payment_gateway.service.auth.RegistrationService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        String apiKey = registrationService.registerMerchant(request);
        return ResponseEntity.ok("Регистрация успешна. Ваш API ключ: " + apiKey);
    }
}
