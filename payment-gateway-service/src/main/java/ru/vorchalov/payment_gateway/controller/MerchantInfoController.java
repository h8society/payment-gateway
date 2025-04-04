package ru.vorchalov.payment_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vorchalov.payment_gateway.dto.MerchantInfoDto;
import ru.vorchalov.payment_gateway.dto.MerchantStatsDto;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/merchant")
public class MerchantInfoController {
    private final UserRepository userRepository;
    private final PaymentTransactionService paymentTransactionService;

    public MerchantInfoController(UserRepository userRepository,
                                  PaymentTransactionService paymentTransactionService) {
        this.userRepository = userRepository;
        this.paymentTransactionService = paymentTransactionService;
    }

    @GetMapping
    public ResponseEntity<MerchantInfoDto> getCurrentMerchant(Authentication auth) {
        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Мерчант не найден"));

        MerchantInfoDto dto = new MerchantInfoDto();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<MerchantStatsDto> getMerchantStats(Authentication auth) {
        MerchantStatsDto stats = paymentTransactionService.getStatsForUser(auth.getName());
        return ResponseEntity.ok(stats);
    }
}
