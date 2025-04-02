package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.TransactionStatusEntity;

import java.util.Optional;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatusEntity, Long> {
    Optional<TransactionStatusEntity> findByStatusCode(String statusCode);
}
