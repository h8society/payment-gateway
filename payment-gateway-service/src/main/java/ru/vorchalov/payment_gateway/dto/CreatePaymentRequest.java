package ru.vorchalov.payment_gateway.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private BigDecimal amount;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

    @NotNull(message = "shopId обязателен")
    private Long shopId;

    @Size(max = 255)
    private String orderNumber;
}