package ru.vorchalov.payment_gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import ru.vorchalov.payment_gateway.dto.MerchantInfoDto;
import ru.vorchalov.payment_gateway.dto.MerchantStatsDto;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MerchantInfoControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentTransactionService transactionService;

    @InjectMocks
    private MerchantInfoController merchantInfoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getCurrentMerchant → 200 OK with merchant info")
    void getCurrentMerchant_ok() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("merchant1");

        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUsername("merchant1");
        user.setEmail("merchant@example.com");

        when(userRepository.findByUsername("merchant1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = merchantInfoController.getCurrentMerchant(auth);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MerchantInfoDto);

        MerchantInfoDto returned = (MerchantInfoDto) response.getBody();
        assertEquals("merchant1", returned.getUsername());
        assertEquals("merchant@example.com", returned.getEmail());
    }

    @Test
    @DisplayName("getMerchantStats → 200 OK with transaction stats")
    void getMerchantStats_ok() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("merchant1");

        MerchantStatsDto stats = new MerchantStatsDto();
        stats.setTransactionCount(3);
        stats.setTotalAmount(new BigDecimal("123.45"));

        when(transactionService.getStatsForUser("merchant1")).thenReturn(stats);

        ResponseEntity<MerchantStatsDto> response = merchantInfoController.getMerchantStats(auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3, response.getBody().getTransactionCount());
        assertEquals(new BigDecimal("123.45"), response.getBody().getTotalAmount());
    }
}
