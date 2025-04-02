package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;

public interface GatewaySettingRepository extends JpaRepository<GatewaySettingEntity, String> {
}
