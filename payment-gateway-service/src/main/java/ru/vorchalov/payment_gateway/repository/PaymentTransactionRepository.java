package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.PaymentTransactionEntity;
import ru.vorchalov.payment_gateway.entity.TransactionStatusEntity;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, String> {
    List<PaymentTransactionEntity> findAllByUserUsername(String username);
    List<PaymentTransactionEntity> findAllByStatus(TransactionStatusEntity status);
}
