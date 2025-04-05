package ru.vorchalov.payment_gateway.service.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vorchalov.payment_gateway.dto.*;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;
import ru.vorchalov.payment_gateway.entity.PaymentTransactionEntity;
import ru.vorchalov.payment_gateway.entity.TransactionStatusEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.PaymentTransactionRepository;
import ru.vorchalov.payment_gateway.repository.TransactionStatusRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepo;
    private final TransactionStatusRepository statusRepo;
    private final GatewaySettingRepository settingRepo;
    private final CardEncryptionService encryptionService;
    private final MrBinLookupService mrBinService;
    private final BankEmulatorService bankEmulatorService;

    public PaymentTransactionService(PaymentTransactionRepository transactionRepo,
                                     TransactionStatusRepository statusRepo,
                                     GatewaySettingRepository settingRepo,
                                     CardEncryptionService encryptionService,
                                     MrBinLookupService mrBinService,
                                     BankEmulatorService bankEmulatorService) {
        this.transactionRepo = transactionRepo;
        this.statusRepo = statusRepo;
        this.settingRepo = settingRepo;
        this.encryptionService = encryptionService;
        this.mrBinService = mrBinService;
        this.bankEmulatorService = bankEmulatorService;
    }

    @Transactional
    public PaymentTransactionDto createTransaction(CreatePaymentRequest req,
                                                   Object user,
                                                   Object key) {
        if (user instanceof UserEntity u && !u.isActive()) {
            throw new RuntimeException("Merchant is blocked");
        }
        TransactionStatusEntity createdStatus = statusRepo.findByStatusCode("created")
                .orElseThrow(() -> new RuntimeException("Status 'created' not found"));

        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        if (user instanceof ru.vorchalov.payment_gateway.entity.UserEntity) {
            entity.setUser((ru.vorchalov.payment_gateway.entity.UserEntity) user);
        }
        if (key instanceof ru.vorchalov.payment_gateway.entity.MerchantKeyEntity) {
            entity.setMerchantKey((ru.vorchalov.payment_gateway.entity.MerchantKeyEntity) key);
        }
        entity.setAmount(req.getAmount() == null ? BigDecimal.ZERO : req.getAmount());
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");

        String transactionId = UUID.randomUUID().toString();

        entity.setTransactionId(transactionId);
        entity.setCardNumberEnc(encryptionService.encrypt(req.getCardNumber()));
        entity.setCardExpiryEnc(encryptionService.encrypt(req.getCardExpiry()));
        entity.setCardCvcEnc(encryptionService.encrypt(req.getCardCvc()));

        transactionRepo.save(entity);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public PaymentTransactionDto getTransactionById(String id) {
        PaymentTransactionEntity entity = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public PaymentTransactionDto payTransaction(String id, PayTransactionRequest req) {
        PaymentTransactionEntity entity = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));

        UserEntity user = entity.getUser();
        if (user != null && !user.isActive()) {
            throw new RuntimeException("Merchant is blocked");
        }

        int ttlMinutes = getPaymentTtlMinutes();
        long minutesSinceCreation = ChronoUnit.MINUTES.between(entity.getTransactionDate(), LocalDateTime.now());
        if (minutesSinceCreation > ttlMinutes) {
            TransactionStatusEntity canceledStatus = statusRepo.findByStatusCode("canceled")
                    .orElseThrow(() -> new RuntimeException("Status 'canceled' not found"));
            entity.setStatus(canceledStatus);
            entity.setResponseCode("TIMEOUT");
            transactionRepo.save(entity);
            return toDto(entity);
        }

        String bin = parseBin(req.getCardNumber());
        MrBinLookupResponse binInfo = mrBinService.lookup(bin);
        entity.setBinBrand(binInfo.getBrand());
        entity.setBinBankName(binInfo.getBank_name());
        entity.setBinCountry(binInfo.getCountry_name());

        String bankResponse = bankEmulatorService.getResponseCode(req.getCardNumber());

        if ("APPROVED".equalsIgnoreCase(bankResponse.trim())) {
            TransactionStatusEntity paidStatus = statusRepo.findByStatusCode("paid")
                    .orElseThrow(() -> new RuntimeException("Status 'paid' not found"));
            entity.setStatus(paidStatus);
            entity.setResponseCode("00");
        } else {
            TransactionStatusEntity declinedStatus = statusRepo.findByStatusCode("declined")
                    .orElseThrow(() -> new RuntimeException("Status 'declined' not found"));
            entity.setStatus(declinedStatus);
            entity.setResponseCode("05");
        }

        entity.setCardNumberEnc(encryptionService.encrypt(req.getCardNumber()));
        entity.setCardExpiryEnc(encryptionService.encrypt(req.getCardExpiry()));
        entity.setCardCvcEnc(encryptionService.encrypt(req.getCardCvc()));

        transactionRepo.save(entity);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsForUser(String username) {
        List<PaymentTransactionEntity> transactions = transactionRepo.findAllByUserUsername(username);
        return transactions.stream().map(this::toDto).toList();
    }

    private String parseBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "000000";
        }
        return cardNumber.length() > 12 ? cardNumber.substring(0, 12) : cardNumber;
    }

    private int getPaymentTtlMinutes() {
        Optional<GatewaySettingEntity> ttlSetting = settingRepo.findById("PAYMENT_TTL_MINUTES");
        if (ttlSetting.isPresent()) {
            return Integer.parseInt(ttlSetting.get().getValue());
        }
        return 15;
    }

    private PaymentTransactionDto toDto(PaymentTransactionEntity e) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(e.getTransactionId());
        dto.setAmount(e.getAmount());
        if (e.getStatus() != null) {
            dto.setStatusCode(e.getStatus().getStatusCode());
        }
        dto.setResponseCode(e.getResponseCode());
        dto.setTransactionDate(e.getTransactionDate());
        dto.setBinBrand(e.getBinBrand());
        dto.setBinBankName(e.getBinBankName());
        dto.setBinCountry(e.getBinCountry());
        return dto;
    }

    public MerchantStatsDto getStatsForUser(String username) {
        List<PaymentTransactionEntity> transactions = transactionRepo.findAllByUserUsername(username);

        long totalCount = transactions.size();

        long paidCount = transactions.stream()
                .filter(tx -> "paid".equalsIgnoreCase(tx.getStatus().getStatusCode()))
                .count();

        long nonPaidCount = totalCount - paidCount;

        BigDecimal totalAmount = transactions.stream()
                .map(PaymentTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = transactions.stream()
                .filter(tx -> "paid".equalsIgnoreCase(tx.getStatus().getStatusCode()))
                .map(PaymentTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MerchantStatsDto dto = new MerchantStatsDto();
        dto.setTransactionCount(totalCount);
        dto.setPaidCount(paidCount);
        dto.setNonPaidCount(nonPaidCount);
        dto.setTotalAmount(totalAmount);
        dto.setPaidAmount(paidAmount);
        return dto;
    }

    public List<PaymentTransactionDto> getAllTransactions() {
        List<PaymentTransactionEntity> entities = transactionRepo.findAll();

        return entities.stream()
                .map(tx -> {
                    PaymentTransactionDto dto = new PaymentTransactionDto();
                    dto.setTransactionId(tx.getTransactionId());
                    dto.setAmount(tx.getAmount());
                    dto.setStatusCode(tx.getStatus().getStatusCode());
                    dto.setResponseCode(tx.getResponseCode());
                    dto.setTransactionDate(tx.getTransactionDate());
                    dto.setBinBrand(tx.getBinBrand());
                    dto.setBinBankName(tx.getBinBankName());
                    dto.setBinCountry(tx.getBinCountry());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
