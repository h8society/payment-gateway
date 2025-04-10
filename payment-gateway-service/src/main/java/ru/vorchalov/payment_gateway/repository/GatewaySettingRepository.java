package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;

import java.util.Optional;

public interface GatewaySettingRepository extends JpaRepository<GatewaySettingEntity, String> {
    Optional<GatewaySettingEntity> findByKey(String key);
}
