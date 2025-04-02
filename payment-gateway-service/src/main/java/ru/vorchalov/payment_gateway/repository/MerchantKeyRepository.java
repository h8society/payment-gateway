package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;

import java.util.Optional;

public interface MerchantKeyRepository extends JpaRepository<MerchantKeyEntity, Long> {
    Optional<MerchantKeyEntity> findByApiKey(String apiKey);
}