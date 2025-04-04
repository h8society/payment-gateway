package ru.vorchalov.payment_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/merchant")
public class MerchantTransactionController {

    private final PaymentTransactionService paymentTransactionService;
    private final UserRepository userRepository;

    public MerchantTransactionController(PaymentTransactionService paymentTransactionService,
                                         UserRepository userRepository) {
        this.paymentTransactionService = paymentTransactionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentTransactionDto>> getMyTransactions(Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется аутентификация");
        }

        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        List<PaymentTransactionDto> transactions = new ArrayList<>(paymentTransactionService.getTransactionsForUser(user.getUsername()));

        transactions.sort(Comparator.comparing(PaymentTransactionDto::getTransactionDate));

        return ResponseEntity.ok(transactions);
    }
}
