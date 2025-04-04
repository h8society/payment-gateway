package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.PaymentTransactionEntity;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {
    List<PaymentTransactionEntity> findByUser_UserId(Long userId);
    List<PaymentTransactionEntity> findByMerchantKey_ApiKey(String apiKey);
    List<PaymentTransactionEntity> findAllByUserUsername(String username);
}
