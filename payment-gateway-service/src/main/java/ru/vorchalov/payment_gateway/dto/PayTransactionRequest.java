package ru.vorchalov.payment_gateway.dto;

public class PayTransactionRequest {
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

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
