package ru.vorchalov.payment_gateway.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MerchantTransactionController.class)
public class MerchantTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentTransactionService paymentTransactionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MerchantKeyRepository merchantKeyRepository;

    @Test
    @WithMockUser(username = "merchant2", roles = {"MERCHANT"})
    @DisplayName("GET /api/merchant/transactions → 200 OK with empty list")
    void getMyTransactions_emptyList() throws Exception {
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("merchant2");

        when(userRepository.findByUsername("merchant2")).thenReturn(Optional.of(mockUser));
        when(paymentTransactionService.getTransactionsForUser("merchant2")).thenReturn(List.of());

        mockMvc.perform(get("/api/merchant/transactions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "merchant2", roles = {"MERCHANT"})
    @DisplayName("GET /api/merchant/transactions → 200 OK with one transaction")
    void getMyTransactions_withOneTransaction() throws Exception {
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("merchant2");

        when(userRepository.findByUsername("merchant2")).thenReturn(Optional.of(mockUser));

        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(10L);
        dto.setAmount(new BigDecimal("199.99"));
        dto.setStatusCode("paid");
        dto.setResponseCode("00");
        dto.setTransactionDate(LocalDateTime.now());
        dto.setBinBrand("VISA");
        dto.setBinBankName("TestBank");
        dto.setBinCountry("USA");

        when(paymentTransactionService.getTransactionsForUser("merchant2"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/merchant/transactions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(10L))
                .andExpect(jsonPath("$[0].amount").value(199.99))
                .andExpect(jsonPath("$[0].statusCode").value("paid"))
                .andExpect(jsonPath("$[0].responseCode").value("00"))
                .andExpect(jsonPath("$[0].binBrand").value("VISA"))
                .andExpect(jsonPath("$[0].binBankName").value("TestBank"))
                .andExpect(jsonPath("$[0].binCountry").value("USA"));
    }

    @Test
    @DisplayName("GET /api/merchant/transactions without auth → 401")
    void getMyTransactions_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/merchant/transactions"))
                .andExpect(status().isUnauthorized());
    }
}
