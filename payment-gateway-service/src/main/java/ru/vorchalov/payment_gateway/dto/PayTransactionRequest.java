package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

@Data
public class PayTransactionRequest {
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

}
