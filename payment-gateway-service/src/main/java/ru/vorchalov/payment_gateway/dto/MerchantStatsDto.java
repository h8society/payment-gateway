package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantStatsDto {
    private long transactionCount;
    private long paidCount;
    private long nonPaidCount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
}
