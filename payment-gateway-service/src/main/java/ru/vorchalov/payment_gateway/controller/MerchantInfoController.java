package ru.vorchalov.payment_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.CreateApiKeyRequest;
import ru.vorchalov.payment_gateway.dto.MerchantInfoDto;
import ru.vorchalov.payment_gateway.dto.MerchantStatsDto;
import ru.vorchalov.payment_gateway.dto.TransactionStatsItemDto;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.auth.RegistrationService;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/merchant")
public class MerchantInfoController {
    private final UserRepository userRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final MerchantKeyRepository merchantKeyRepository;
    private final RegistrationService registrationService;

    public MerchantInfoController(UserRepository userRepository,
                                  PaymentTransactionService paymentTransactionService,
                                  MerchantKeyRepository merchantKeyRepository,
                                  RegistrationService registrationService) {
        this.userRepository = userRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.merchantKeyRepository = merchantKeyRepository;
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<MerchantInfoDto> getCurrentMerchant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Мерчант не найден"));

        Optional<List<MerchantKeyEntity>> apiKeys = merchantKeyRepository.findAllByUser(user);

        MerchantInfoDto dto = new MerchantInfoDto();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(r -> r.getRoleName().replace("ROLE_", ""))
                .toList());
        dto.setApiKeys(apiKeys
                .orElse(List.of())
                .stream()
                .map(MerchantKeyEntity::getApiKey)
                .toList());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<MerchantStatsDto> getMerchantStats(Authentication auth) {
        MerchantStatsDto stats = paymentTransactionService.getStatsForUser(auth.getName());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/graphics")
    public ResponseEntity<List<TransactionStatsItemDto>> getMerchantStats(
            Authentication auth,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long statusId
    ) {
        UserEntity merchant = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        List<TransactionStatsItemDto> stats = paymentTransactionService.getTransactionStats(merchant, shopId, statusId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/keys")
    public ResponseEntity<String> createApiKey(@RequestBody CreateApiKeyRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Мерчант не найден"));

        MerchantKeyEntity newKey = registrationService.generateApiKeyForUser(user, request.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body("""
        {
          "apiKey": "%s"
        }
        """.formatted(newKey.getApiKey()));
    }

    @DeleteMapping("/keys/{key}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable String key, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = auth.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Мерчант не найден"));

        Optional<MerchantKeyEntity> maybeKey = merchantKeyRepository.findByApiKey(key);

        if (maybeKey.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MerchantKeyEntity merchantKey = maybeKey.get();

        if (!merchantKey.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        merchantKeyRepository.delete(merchantKey);
        return ResponseEntity.noContent().build();
    }
}
