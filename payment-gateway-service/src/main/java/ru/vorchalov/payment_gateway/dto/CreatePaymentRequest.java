package ru.vorchalov.payment_gateway.dto;

import java.math.BigDecimal;

public class CreatePaymentRequest {
    private BigDecimal amount;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public String getCardExpiry() {
        return cardExpiry;
    }
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }
    public String getCardCvc() {
        return cardCvc;
    }
    public void setCardCvc(String cardCvc) {
        this.cardCvc = cardCvc;
    }
}
