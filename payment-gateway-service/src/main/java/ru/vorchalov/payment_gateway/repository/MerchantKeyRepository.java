package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.MerchantKeyEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface MerchantKeyRepository extends JpaRepository<MerchantKeyEntity, Long> {
    Optional<MerchantKeyEntity> findByApiKey(String apiKey);
    Optional<List<MerchantKeyEntity>> findAllByUser(UserEntity user);
}