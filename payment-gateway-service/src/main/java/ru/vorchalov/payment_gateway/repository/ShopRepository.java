package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.ShopEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
    List<ShopEntity> findAllByMerchant(UserEntity merchant);

    Optional<ShopEntity> findByShopIdAndMerchant(Long shopId, UserEntity merchant);
}
