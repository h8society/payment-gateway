package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private BigDecimal amount;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;
}
