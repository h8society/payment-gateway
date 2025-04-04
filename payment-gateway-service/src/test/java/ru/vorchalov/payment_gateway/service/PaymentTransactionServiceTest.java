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
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.PaymentTransactionRepository;
import ru.vorchalov.payment_gateway.repository.TransactionStatusRepository;
import ru.vorchalov.payment_gateway.service.payment.BankEmulatorService;
import ru.vorchalov.payment_gateway.service.payment.CardEncryptionService;
import ru.vorchalov.payment_gateway.service.payment.MrBinLookupService;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        entity.setTransactionId(1L);
        entity.setAmount(BigDecimal.valueOf(50.00));
        TransactionStatusEntity status = new TransactionStatusEntity();
        status.setStatusCode("created");
        entity.setStatus(status);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now());
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(entity));

        PaymentTransactionDto dto = paymentService.getTransactionById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getTransactionId());
        assertEquals(BigDecimal.valueOf(50.00), dto.getAmount());
        assertEquals("created", dto.getStatusCode());
        assertEquals("PENDING", dto.getResponseCode());
    }

    @Test
    void testPayTransaction_TTLExpired() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.setTransactionId(2L);
        entity.setAmount(BigDecimal.valueOf(200.00));
        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now().minusMinutes(20));
        when(transactionRepo.findById(2L)).thenReturn(Optional.of(entity));

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

        PaymentTransactionDto dto = paymentService.payTransaction(2L, req);

        assertNotNull(dto);
        assertEquals("canceled", dto.getStatusCode());
        assertEquals("TIMEOUT", dto.getResponseCode());
    }

    @Test
    void testPayTransaction_Success() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.setTransactionId(3L);
        entity.setAmount(BigDecimal.valueOf(300.00));
        TransactionStatusEntity createdStatus = new TransactionStatusEntity();
        createdStatus.setStatusCode("created");
        entity.setStatus(createdStatus);
        entity.setResponseCode("PENDING");
        entity.setTransactionDate(LocalDateTime.now().minusMinutes(5));
        when(transactionRepo.findById(3L)).thenReturn(Optional.of(entity));

        GatewaySettingEntity ttlSetting = new GatewaySettingEntity();
        ttlSetting.setKey("PAYMENT_TTL_MINUTES");
        ttlSetting.setValue("15");
        when(settingRepo.findById("PAYMENT_TTL_MINUTES")).thenReturn(Optional.of(ttlSetting));

        MrBinLookupResponse binResponse = new MrBinLookupResponse();
        binResponse.setBrand("VISA");
        binResponse.setBank_name("Test Bank");
        binResponse.setCountry_name("USA");
        when(mrBinService.lookup(any())).thenReturn(binResponse);

        // Твой эмулятор возвращает APPROVED, не "00"
        when(bankEmulatorService.getResponseCode(any())).thenReturn("APPROVED");

        // Вот тут важно: убедись, что статус paid возвращается
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

        PaymentTransactionDto dto = paymentService.payTransaction(3L, req);

        assertNotNull(dto);
        assertEquals("paid", dto.getStatusCode());
        assertEquals("00", dto.getResponseCode());
        assertEquals("VISA", dto.getBinBrand());
        assertEquals("Test Bank", dto.getBinBankName());
        assertEquals("USA", dto.getBinCountry());
    }

}
