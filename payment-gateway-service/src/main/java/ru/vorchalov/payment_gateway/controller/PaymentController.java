package ru.vorchalov.payment_gateway.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.CreatePaymentRequest;
import ru.vorchalov.payment_gateway.dto.PayTransactionRequest;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.dto.StatusDto;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;
import ru.vorchalov.payment_gateway.entity.TransactionStatusEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.TransactionStatusRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentTransactionService paymentService;
    private final MerchantKeyRepository merchantKeyRepo;
    private final UserRepository userRepo;
    private final TransactionStatusRepository transactionStatusRepo;

    public PaymentController(PaymentTransactionService paymentService,
                             MerchantKeyRepository merchantKeyRepo,
                             UserRepository userRepo,
                             TransactionStatusRepository transactionStatusRepo) {
        this.paymentService = paymentService;
        this.merchantKeyRepo = merchantKeyRepo;
        this.userRepo = userRepo;
        this.transactionStatusRepo = transactionStatusRepo;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                             Authentication auth,
                                                             @RequestHeader(name = "X-Api-Key", required = false) String apiKey,
                                                             @Value("${payment.form.url}") String paymentFormUrl) {
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

        Map<String, String> response = new HashMap<>();
        response.put("transactionId", dto.getTransactionId().toString());
        response.put("formUrl", paymentFormUrl + dto.getTransactionId().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionDto> getPayment(@PathVariable String id) {
        PaymentTransactionDto dto = paymentService.getTransactionById(id);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Платёж не найден");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentTransactionDto> payTransaction(@PathVariable String id,
                                                                @Valid @RequestBody PayTransactionRequest request) {
        PaymentTransactionDto dto = paymentService.payTransaction(id, request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentTransactionDto> refundTransaction(@PathVariable String id) {
        PaymentTransactionDto refundDto = paymentService.refundTransaction(id);
        return ResponseEntity.ok(refundDto);
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<StatusDto>> getStatuses() {
        List<TransactionStatusEntity> statusEntities = transactionStatusRepo.findAll();
        List<StatusDto> statuses = statusEntities.stream()
                .map(entity -> new StatusDto(entity.getStatusId(), entity.getDescription(), entity.getStatusCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }

}
