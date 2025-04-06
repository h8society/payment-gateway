package ru.vorchalov.payment_gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;
import ru.vorchalov.payment_gateway.dto.CreatePaymentRequest;
import ru.vorchalov.payment_gateway.dto.PayTransactionRequest;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.dto.MrBinLookupResponse;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;
import ru.vorchalov.payment_gateway.entity.PaymentTransactionEntity;
import ru.vorchalov.payment_gateway.entity.TransactionStatusEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.PaymentTransactionRepository;
import ru.vorchalov.payment_gateway.repository.TransactionStatusRepository;
import ru.vorchalov.payment_gateway.service.payment.BankEmulatorService;
import ru.vorchalov.payment_gateway.service.payment.CardEncryptionService;
import ru.vorchalov.payment_gateway.service.payment.MrBinLookupService;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class PaymentTransactionServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepo;

    @Mock
    private TransactionStatusRepository statusRepo;

    @Mock
    private GatewaySettingRepository settingRepo;

    @Mock
    private CardEncryptionService encryptionService;

    @Mock
    private MrBinLookupService mrBinService;

    @Mock
    private BankEmulatorService bankEmulatorService;

    @InjectMocks
    private PaymentTransactionService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTransaction() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(BigDecimal.valueOf(100.00));
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        when(statusRepo.findByStatusCode("created")).thenReturn(Optional.of(createdStatus));
        when(encryptionService.encrypt(any())).thenAnswer(invocation -> "ENC(" + invocation.getArgument(0) + ")");
        when(transactionRepo.save(any(PaymentTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentTransactionDto dto = paymentService.createTransaction(req, null, null);

        assertNotNull(dto);
        assertEquals(BigDecimal.valueOf(100.00), dto.getAmount());
        assertEquals("created", dto.getStatusCode());
        assertEquals("PENDING", dto.getResponseCode());
        verify(encryptionService, times(3)).encrypt(any());
        verify(transactionRepo, times(1)).save(any(PaymentTransactionEntity.class));
    }

    @Test
    void testGetTransactionById() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        String id = UUID.randomUUID().toString();
        entity.setTransactionId(id);
        entity.setAmount(BigDecimal.valueOf(50.00));
        TransactionStatusEntity status = new TransactionStatusEntity();
        status.setStatusCode("created");
        entity.setStatus(status);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now());
        when(transactionRepo.findById(id)).thenReturn(Optional.of(entity));

        PaymentTransactionDto dto = paymentService.getTransactionById(id);

        assertNotNull(dto);
        assertEquals(id, dto.getTransactionId());
        assertEquals(BigDecimal.valueOf(50.00), dto.getAmount());
        assertEquals("created", dto.getStatusCode());
        assertEquals("PENDING", dto.getResponseCode());
    }

    @Test
    void testPayTransaction_TTLExpired() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        String id = UUID.randomUUID().toString();
        entity.setTransactionId(id);
        entity.setAmount(BigDecimal.valueOf(200.00));
        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now().minusMinutes(20));
        when(transactionRepo.findById(id)).thenReturn(Optional.of(entity));

        GatewaySettingEntity ttlSetting = new GatewaySettingEntity();
        ttlSetting.setKey("PAYMENT_TTL_MINUTES");
        ttlSetting.setValue("15");
        when(settingRepo.findById("PAYMENT_TTL_MINUTES")).thenReturn(Optional.of(ttlSetting));

        TransactionStatusEntity canceledStatus = new TransactionStatusEntity();
        canceledStatus.setStatusCode("canceled");
        when(statusRepo.findByStatusCode("canceled")).thenReturn(Optional.of(canceledStatus));

        PayTransactionRequest req = new PayTransactionRequest();
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        PaymentTransactionDto dto = paymentService.payTransaction(id, req);

        assertNotNull(dto);
        assertEquals("canceled", dto.getStatusCode());
        assertEquals("TIMEOUT", dto.getResponseCode());
    }

    @Test
    void testPayTransaction_Success() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        String id = UUID.randomUUID().toString();
        entity.setTransactionId(id);
        entity.setAmount(BigDecimal.valueOf(300.00));
        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now().minusMinutes(5));
        when(transactionRepo.findById(id)).thenReturn(Optional.of(entity));

        GatewaySettingEntity ttlSetting = new GatewaySettingEntity();
        ttlSetting.setKey("PAYMENT_TTL_MINUTES");
        ttlSetting.setValue("15");
        when(settingRepo.findById("PAYMENT_TTL_MINUTES")).thenReturn(Optional.of(ttlSetting));

        MrBinLookupResponse binResponse = new MrBinLookupResponse();
        binResponse.setBrand("VISA");
        binResponse.setBank_name("Test Bank");
        binResponse.setCountry_name("USA");
        when(mrBinService.lookup(any())).thenReturn(binResponse);

        when(bankEmulatorService.getResponseCode(any())).thenReturn("APPROVED");

        TransactionStatusEntity paidStatus = new TransactionStatusEntity();
        paidStatus.setStatusCode("paid");
        when(statusRepo.findByStatusCode("paid")).thenReturn(Optional.of(paidStatus));

        TransactionStatusEntity declinedStatus = new TransactionStatusEntity();
        declinedStatus.setStatusCode("declined");
        when(statusRepo.findByStatusCode("declined")).thenReturn(Optional.of(declinedStatus));

        when(encryptionService.encrypt(any())).thenAnswer(invocation -> "ENC(" + invocation.getArgument(0) + ")");
        when(transactionRepo.save(any(PaymentTransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayTransactionRequest req = new PayTransactionRequest();
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        PaymentTransactionDto dto = paymentService.payTransaction(id, req);

        assertNotNull(dto);
        assertEquals("paid", dto.getStatusCode());
        assertEquals("00", dto.getResponseCode());
        assertEquals("VISA", dto.getBinBrand());
        assertEquals("Test Bank", dto.getBinBankName());
        assertEquals("USA", dto.getBinCountry());
    }

    @Test
    void getTransactionsForUser_returnsMappedDtos() {
        PaymentTransactionEntity tx = new PaymentTransactionEntity();
        String id = UUID.randomUUID().toString();
        tx.setTransactionId(id);
        tx.setAmount(new BigDecimal("99.99"));
        tx.setResponseCode("00");
        tx.setTransactionDate(LocalDateTime.now());
        tx.setBinBrand("VISA");
        tx.setBinBankName("Test Bank");
        tx.setBinCountry("Germany");

        TransactionStatusEntity status = new TransactionStatusEntity();
        status.setStatusCode("paid");
        tx.setStatus(status);

        when(transactionRepo.findAllByUserUsername("merchant1"))
                .thenReturn(List.of(tx));

        List<PaymentTransactionDto> result = paymentService.getTransactionsForUser("merchant1");

        assertEquals(1, result.size());
        PaymentTransactionDto dto = result.get(0);
        assertEquals(id, dto.getTransactionId());
        assertEquals(new BigDecimal("99.99"), dto.getAmount());
        assertEquals("paid", dto.getStatusCode());
        assertEquals("00", dto.getResponseCode());
        assertEquals("VISA", dto.getBinBrand());
        assertEquals("Test Bank", dto.getBinBankName());
        assertEquals("Germany", dto.getBinCountry());
    }

    @Test
    void getTransactionsForUser_whenNone_returnsEmptyList() {
        when(transactionRepo.findAllByUserUsername("emptyuser"))
                .thenReturn(List.of());

        List<PaymentTransactionDto> result = paymentService.getTransactionsForUser("emptyuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStatsForUser_returnsCorrectStats() {
        PaymentTransactionEntity paidTx1 = new PaymentTransactionEntity();
        paidTx1.setAmount(new BigDecimal("100.00"));
        TransactionStatusEntity paidStatus = new TransactionStatusEntity();
        paidStatus.setStatusCode("paid");
        paidTx1.setStatus(paidStatus);

        PaymentTransactionEntity declinedTx = new PaymentTransactionEntity();
        declinedTx.setAmount(new BigDecimal("200.00"));
        TransactionStatusEntity declinedStatus = new TransactionStatusEntity();
        declinedStatus.setStatusCode("declined");
        declinedTx.setStatus(declinedStatus);

        PaymentTransactionEntity paidTx2 = new PaymentTransactionEntity();
        paidTx2.setAmount(new BigDecimal("300.00"));
        paidTx2.setStatus(paidStatus);

        when(transactionRepo.findAllByUserUsername("merchant1"))
                .thenReturn(List.of(paidTx1, declinedTx, paidTx2));

        var stats = paymentService.getStatsForUser("merchant1");

        assertNotNull(stats);
        assertEquals(3, stats.getTransactionCount());
        assertEquals(2, stats.getPaidCount());
        assertEquals(1, stats.getNonPaidCount());
        assertEquals(new BigDecimal("600.00"), stats.getTotalAmount());
        assertEquals(new BigDecimal("400.00"), stats.getPaidAmount());
    }

    @Test
    void testPayTransaction_whenUserBlocked_shouldThrow() {
        PaymentTransactionEntity tx = new PaymentTransactionEntity();
        String id = UUID.randomUUID().toString();
        tx.setTransactionId(id);
        tx.setAmount(BigDecimal.TEN);
        tx.setTransactionDate(LocalDateTime.now());
        TransactionStatusEntity status = new TransactionStatusEntity();
        status.setStatusCode("created");
        tx.setStatus(status);

        UserEntity user = new UserEntity();
        user.setActive(false);
        tx.setUser(user);

        when(transactionRepo.findById(id)).thenReturn(Optional.of(tx));

        PayTransactionRequest req = new PayTransactionRequest();
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.payTransaction(id, req));

        assertEquals("Merchant is blocked", ex.getMessage());
    }

    @Test
    void testAutoDeclineExpiredTransactions() {
        PaymentTransactionEntity tx1 = new PaymentTransactionEntity();
        tx1.setTransactionId("tx1");
        tx1.setTransactionDate(LocalDateTime.now().minusMinutes(20));
        tx1.setResponseCode("PENDING");

        PaymentTransactionEntity tx2 = new PaymentTransactionEntity();
        tx2.setTransactionId("tx2");
        tx2.setTransactionDate(LocalDateTime.now().minusMinutes(5));
        tx2.setResponseCode("PENDING");

        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        TransactionStatusEntity declinedStatus = new TransactionStatusEntity();
        declinedStatus.setStatusCode("declined");

        tx1.setStatus(createdStatus);
        tx2.setStatus(createdStatus);

        when(statusRepo.findByStatusCode("created")).thenReturn(Optional.of(createdStatus));
        when(statusRepo.findByStatusCode("declined")).thenReturn(Optional.of(declinedStatus));
        when(transactionRepo.findAllByStatus(createdStatus)).thenReturn(List.of(tx1, tx2));
        when(settingRepo.findById("PAYMENT_TTL_MINUTES")).thenReturn(
                Optional.of(new GatewaySettingEntity() {{
                    setKey("PAYMENT_TTL_MINUTES");
                    setValue("15");
                }})
        );

        paymentService.autoDeclineExpiredTransactions();

        assertEquals("declined", tx1.getStatus().getStatusCode());
        assertEquals("EXPIRED", tx1.getResponseCode());

        assertEquals("created", tx2.getStatus().getStatusCode());
        assertEquals("PENDING", tx2.getResponseCode());

        verify(transactionRepo, times(1)).saveAll(List.of(tx1));
    }

}
