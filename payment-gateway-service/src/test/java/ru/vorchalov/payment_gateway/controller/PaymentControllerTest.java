package ru.vorchalov.payment_gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.vorchalov.payment_gateway.dto.CreatePaymentRequest;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.dto.PayTransactionRequest;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@WithMockUser
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentTransactionService paymentService;

    @MockBean
    private MerchantKeyRepository merchantKeyRepo;

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePayment() throws Exception {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(BigDecimal.valueOf(100.00));
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        LocalDateTime fixedDate = LocalDateTime.of(2025, 4, 3, 0, 0);

        String id = UUID.randomUUID().toString();

        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(id);
        dto.setAmount(req.getAmount());
        dto.setStatusCode("created");
        dto.setResponseCode("PENDING");
        dto.setTransactionDate(fixedDate);

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(1L);
        mockUser.setUsername("user");
        mockUser.setEmail("user@example.com");
        mockUser.setHashedPassword("hashed_password");

        when(userRepo.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(paymentService.createTransaction(any(CreatePaymentRequest.class), any(), any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(id)))
                .andExpect(jsonPath("$.formUrl", is("http://localhost:5173/pay/" + id)));
    }

    @Test
    void testGetPayment() throws Exception {
        LocalDateTime fixedDate = LocalDateTime.of(2025, 4, 3, 0, 0);

        String id = UUID.randomUUID().toString();

        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(id);
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setStatusCode("created");
        dto.setResponseCode("PENDING");
        dto.setTransactionDate(fixedDate);

        when(paymentService.getTransactionById(eq(id))).thenReturn(dto);

        mockMvc.perform(get("/api/payments/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(id)))
                .andExpect(jsonPath("$.statusCode", is("created")));
    }

    @Test
    void testPayTransaction() throws Exception {
        PayTransactionRequest req = new PayTransactionRequest();
        req.setCardNumber("4111111111111111");
        req.setCardExpiry("12/25");
        req.setCardCvc("123");

        LocalDateTime fixedDate = LocalDateTime.of(2025, 4, 3, 0, 0);

        String id = UUID.randomUUID().toString();

        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(id);
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setStatusCode("paid");
        dto.setResponseCode("00");
        dto.setTransactionDate(fixedDate);

        when(paymentService.payTransaction(eq(id), any(PayTransactionRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/payments/" + id + "/pay")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(id)))
                .andExpect(jsonPath("$.statusCode", is("paid")));
    }
}
