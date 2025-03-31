package ru.vorchalov.bank_emulator.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

@Component
public class SecretValidator {

    @Value("${bank.emulator.secret}")
    private String configuredSecret;

    public void validate(String headerSecret) {
        if (headerSecret == null || !headerSecret.equals(configuredSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Invalid Secret");
        }
    }
}
