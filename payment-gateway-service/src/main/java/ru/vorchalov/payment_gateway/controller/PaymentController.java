package ru.vorchalov.payment_gateway.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.CreatePaymentRequest;
import ru.vorchalov.payment_gateway.dto.PayTransactionRequest;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentTransactionService paymentService;
    private final MerchantKeyRepository merchantKeyRepo;
    private final UserRepository userRepo;

    public PaymentController(PaymentTransactionService paymentService,
                             MerchantKeyRepository merchantKeyRepo,
                             UserRepository userRepo) {
        this.paymentService = paymentService;
        this.merchantKeyRepo = merchantKeyRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<PaymentTransactionDto> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                               Authentication auth,
                                                               @RequestHeader(name = "X-Api-Key", required = false) String apiKey) {
        MerchantKeyEntity key = null;
        if (apiKey != null) {
            key = merchantKeyRepo.findByApiKey(apiKey)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный API ключ"));
        }

        UserEntity user = null;
        if (auth != null) {
            user = userRepo.findByUsername(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        }

        if (key == null && user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация или API ключ");
        }

        PaymentTransactionDto dto = paymentService.createTransaction(request, user, key);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionDto> getPayment(@PathVariable Long id) {
        PaymentTransactionDto dto = paymentService.getTransactionById(id);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Платёж не найден");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentTransactionDto> payTransaction(@PathVariable Long id,
                                                                @Valid @RequestBody PayTransactionRequest request) {
        PaymentTransactionDto dto = paymentService.payTransaction(id, request);
        return ResponseEntity.ok(dto);
    }
}
