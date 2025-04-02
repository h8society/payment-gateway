package ru.vorchalov.payment_gateway.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id")
    private MerchantKeyEntity merchantKey;

    @Column(nullable = false)
    private String cardNumberEnc;

    @Column(nullable = false)
    private String cardExpiryEnc;

    @Column(nullable = false)
    private String cardCvcEnc;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String responseCode;

    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private TransactionStatusEntity status;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public MerchantKeyEntity getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(MerchantKeyEntity merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getCardNumberEnc() {
        return cardNumberEnc;
    }

    public void setCardNumberEnc(String cardNumberEnc) {
        this.cardNumberEnc = cardNumberEnc;
    }

    public String getCardExpiryEnc() {
        return cardExpiryEnc;
    }

    public void setCardExpiryEnc(String cardExpiryEnc) {
        this.cardExpiryEnc = cardExpiryEnc;
    }

    public String getCardCvcEnc() {
        return cardCvcEnc;
    }

    public void setCardCvcEnc(String cardCvcEnc) {
        this.cardCvcEnc = cardCvcEnc;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public TransactionStatusEntity getStatus() {
        return status;
    }

    public void setStatus(TransactionStatusEntity status) {
        this.status = status;
    }
}
