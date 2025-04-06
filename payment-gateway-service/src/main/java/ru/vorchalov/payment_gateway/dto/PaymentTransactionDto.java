package ru.vorchalov.payment_gateway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransactionDto {
    private String transactionId;
    private BigDecimal amount;
    private String statusCode;
    private String responseCode;
    private LocalDateTime transactionDate;
    private String binBrand;
    private String binBankName;
    private String binCountry;
    private LocalDateTime expiredAt;

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getBinBrand() {
        return binBrand;
    }
    public void setBinBrand(String binBrand) {
        this.binBrand = binBrand;
    }
    public String getBinBankName() {
        return binBankName;
    }
    public void setBinBankName(String binBankName) {
        this.binBankName = binBankName;
    }
    public String getBinCountry() {
        return binCountry;
    }
    public void setBinCountry(String binCountry) {
        this.binCountry = binCountry;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
