package ru.vorchalov.bank_emulator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vorchalov.bank_emulator.entity.CardResponseMapping;
import ru.vorchalov.bank_emulator.service.CardResponseMappingService;
import ru.vorchalov.bank_emulator.security.SecretValidator;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/mappings")
public class CardResponseMappingController {

    private final CardResponseMappingService service;

    private final SecretValidator secretValidator;

    public CardResponseMappingController(CardResponseMappingService service, SecretValidator secretValidator) {
        this.service = service;
        this.secretValidator = secretValidator;
    }

    // Получить код ответа по номеру карты
    @GetMapping("/response-code")
    public ResponseEntity<String> getResponseCode(@RequestParam("cardNumber") String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("cardNumber должен быть длиной 16 символов");
        }

        String responseCode = service.getResponseCodeByCardNumber(cardNumber);
        return ResponseEntity.ok(responseCode);
    }

    // Создать новую запись
    @PostMapping
    public ResponseEntity<CardResponseMapping> createMapping(
            @RequestBody CardResponseMapping request,
            @RequestHeader(name = "X-Secret-777", required = false) String secret) {
        secretValidator.validate(secret);

        if (request.getCardNumber() == null || request.getResponseCode() == null) {
            throw new IllegalArgumentException("cardNumber и responseCode обязательны");
        }

        if (request.getCardNumber().length() != 16) {
            throw new IllegalArgumentException("cardNumber должен быть длиной 16 символов");
        }

        CardResponseMapping created = service.createMapping(
                request.getCardNumber(),
                request.getResponseCode(),
                request.getDescription()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Обновить код ответа по номеру карты
    @PutMapping("/{cardNumber}")
    public ResponseEntity<Void> updateResponseCode(
            @RequestHeader(name = "X-Secret-777", required = false) String secret,
            @PathVariable String cardNumber,
            @RequestParam("newCode") String newCode
    ) {
        secretValidator.validate(secret);

        if (newCode == null || newCode.isBlank()) {
            throw new IllegalArgumentException("newCode обязателен");
        }

        boolean updated = service.updateResponseCode(cardNumber, newCode);
        if (!updated) {
            throw new IllegalArgumentException("Карта не найдена: " + cardNumber);
        }

        return ResponseEntity.noContent().build();
    }

    // Удалить запись по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(
            @RequestHeader(name = "X-Secret-777", required = false) String secret,
            @PathVariable Long id) {
        secretValidator.validate(secret);
        Optional<CardResponseMapping> existing = service.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Запись с id=" + id + " не найдена");
        }
        service.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}
