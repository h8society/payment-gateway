package ru.vorchalov.payment_gateway.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.*;
import ru.vorchalov.payment_gateway.entity.*;
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.PaymentTransactionRepository;
import ru.vorchalov.payment_gateway.repository.ShopRepository;
import ru.vorchalov.payment_gateway.repository.TransactionStatusRepository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepo;
    private final TransactionStatusRepository statusRepo;
    private final GatewaySettingRepository settingRepo;
    private final CardEncryptionService encryptionService;
    private final MrBinLookupService mrBinService;
    private final BankEmulatorService bankEmulatorService;
    private final ShopRepository shopRepository;

    private final JdbcTemplate jdbcTemplate;

    public PaymentTransactionService(PaymentTransactionRepository transactionRepo,
                                     TransactionStatusRepository statusRepo,
                                     GatewaySettingRepository settingRepo,
                                     CardEncryptionService encryptionService,
                                     MrBinLookupService mrBinService,
                                     BankEmulatorService bankEmulatorService,
                                     ShopRepository shopRepository,
                                     JdbcTemplate jdbcTemplate) {
        this.transactionRepo = transactionRepo;
        this.statusRepo = statusRepo;
        this.settingRepo = settingRepo;
        this.encryptionService = encryptionService;
        this.mrBinService = mrBinService;
        this.bankEmulatorService = bankEmulatorService;
        this.shopRepository = shopRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public PaymentTransactionDto createTransaction(CreatePaymentRequest request, UserEntity user, MerchantKeyEntity key) {
        UserEntity merchant = user != null ? user : key.getUser();

        ShopEntity shop = shopRepository.findByShopIdAndMerchant(request.getShopId(), merchant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Магазин не найден или не принадлежит вам"));

        boolean orderNumberRequired = settingRepo
                .findByKey("ORDER_NUMBER_REQUIRED")
                .map(setting -> "true".equalsIgnoreCase(setting.getValue()))
                .orElse(false);

        String orderNumber = request.getOrderNumber();
        if (orderNumberRequired && (orderNumber == null || orderNumber.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderNumber обязателен");
        }

        if (!orderNumberRequired && (orderNumber == null || orderNumber.isBlank())) {
            Long next = jdbcTemplate.queryForObject("SELECT nextval('order_number_seq')", Long.class);
            orderNumber = String.valueOf(next);
        }

        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        TransactionStatusEntity createdStatus = statusRepo.findByStatusCode("created")
                .orElseThrow(() -> new RuntimeException("Status 'created' not found"));
        entity.setAmount(request.getAmount());
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");
        entity.setUser(merchant);
        entity.setShop(shop);
        entity.setOrderNumber(orderNumber);
        entity.setTransactionDate(LocalDateTime.now());

        if (key != null) {
            entity.setMerchantKey(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отсутствует API ключ");
        }

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

    @Transactional
    public void autoDeclineExpiredTransactions() {
        int ttlMinutes = getPaymentTtlMinutes();
        LocalDateTime now = LocalDateTime.now();

        TransactionStatusEntity createdStatus = statusRepo.findByStatusCode("created")
                .orElseThrow(() -> new RuntimeException("Status 'created' not found"));
        TransactionStatusEntity declinedStatus = statusRepo.findByStatusCode("declined")
                .orElseThrow(() -> new RuntimeException("Status 'declined' not found"));

        List<PaymentTransactionEntity> expired = transactionRepo.findAllByStatus(createdStatus).stream()
                .filter(tx -> tx.getTransactionDate().plusMinutes(ttlMinutes).isBefore(now))
                .toList();

        for (PaymentTransactionEntity tx : expired) {
            tx.setStatus(declinedStatus);
            tx.setResponseCode("EXPIRED");
        }

        log.info("🔻 Отклонено {} просроченных транзакций", expired.size());

        transactionRepo.saveAll(expired);
    }

    @Transactional
    public PaymentTransactionDto refundTransaction(String id) {
        PaymentTransactionEntity original = transactionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Транзакция не найдена"));

        if (!"paid".equalsIgnoreCase(original.getStatus().getStatusCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Можно вернуть только оплаченные транзакции");
        }

        TransactionStatusEntity refundStatus = statusRepo.findByStatusCode("refund")
                .orElseThrow(() -> new RuntimeException("Статус 'refund' не найден"));

        PaymentTransactionEntity refund = new PaymentTransactionEntity();
        refund.setAmount(original.getAmount());
        refund.setStatus(refundStatus);
        refund.setUser(original.getUser());
        refund.setShop(original.getShop());
        refund.setTransactionDate(LocalDateTime.now());
        refund.setOrderNumber("REFUND-" + original.getOrderNumber());
        refund.setOriginalTransaction(original);
        refund.setMerchantKey(original.getMerchantKey());
        refund.setCardNumberEnc(original.getCardNumberEnc());
        refund.setCardExpiryEnc(original.getCardExpiryEnc());
        refund.setCardCvcEnc(original.getCardCvcEnc());
        refund.setResponseCode("00");

        transactionRepo.save(refund);

        return toDto(refund);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsForUserWithFilters(String username, String order, Long shopId, Long statusId) {
        List<PaymentTransactionEntity> transactions = transactionRepo.findAllByUserUsername(username);

        return transactions.stream()
                .filter(tx -> order == null
                        || (tx.getOrderNumber() != null && tx.getOrderNumber().toLowerCase().contains(order.toLowerCase())))
                .filter(tx -> shopId == null
                        || (tx.getShop() != null && tx.getShop().getShopId().equals(shopId)))
                .filter(tx -> statusId == null
                        || (tx.getStatus() != null && tx.getStatus().getStatusId().equals(statusId)))
                .sorted(Comparator.comparing(PaymentTransactionEntity::getTransactionDate).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
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
        dto.setOrderNumber(e.getOrderNumber());
        dto.setShopName(e.getShop().getName());

        int ttlMinutes = getPaymentTtlMinutes();
        dto.setExpiredAt(e.getTransactionDate().plusMinutes(ttlMinutes));

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

    public List<TransactionStatsItemDto> getTransactionStats(UserEntity merchant, Long shopId, Long statusId) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT DATE(transaction_date) as date, ");
        sqlBuilder.append("       COUNT(*) as count, ");
        sqlBuilder.append("       SUM(amount) as total ");
        sqlBuilder.append("FROM payment_transactions ");
        sqlBuilder.append("WHERE user_id = ? ");

        List<Object> argList = new ArrayList<>();
        List<Integer> typeList = new ArrayList<>();

        argList.add(merchant.getUserId());
        typeList.add(Types.BIGINT);

        if (shopId != null) {
            sqlBuilder.append("AND shop_id = ? ");
            argList.add(shopId);
            typeList.add(Types.BIGINT);
        }

        if (statusId != null) {
            sqlBuilder.append("AND status_id = ? ");
            argList.add(statusId);
            typeList.add(Types.BIGINT);
        }

        sqlBuilder.append("GROUP BY DATE(transaction_date) ");
        sqlBuilder.append("ORDER BY date ASC");

        String sql = sqlBuilder.toString();

        return jdbcTemplate.query(
                sql,
                argList.toArray(),
                typeList.stream().mapToInt(i -> i).toArray(),
                (rs, rowNum) -> new TransactionStatsItemDto(
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("count"),
                        rs.getBigDecimal("total")
                )
        );
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
