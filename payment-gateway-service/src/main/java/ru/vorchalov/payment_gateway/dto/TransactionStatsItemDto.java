package ru.vorchalov.payment_gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionStatsItemDto {
    private LocalDate date;
    private long count;
    private BigDecimal total;

    public TransactionStatsItemDto(LocalDate date, long count, BigDecimal total) {
        this.date = date;
        this.count = count;
        this.total = total;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
