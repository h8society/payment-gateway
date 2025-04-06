package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentTransaction {
    private Long transactionId;
    private BigDecimal amount;
    private String statusCode;
    private String responseCode;
    private LocalDateTime transactionDate;
    private String binBrand;
    private String binBankName;
    private String binCountry;

}
