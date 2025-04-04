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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "key_id")
    private MerchantKeyEntity merchantKey;

    @Column(nullable = false)
    private String cardNumberEnc;

    @Column(nullable = false)
    private String cardExpiryEnc;

    @Column(nullable = false)
    private String cardCvcEnc;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String responseCode;

    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private TransactionStatusEntity status;

    // Ниже поля, куда будем складывать результат mrbin.io
    @Column
    private String binBrand;

    @Column
    private String binBankName;

    @Column
    private String binCountry;

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

    public void setTransactionDate (LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionStatusEntity getStatus() {
        return status;
    }

    public void setStatus(TransactionStatusEntity status) {
        this.status = status;
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
}
