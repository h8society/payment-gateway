package ru.vorchalov.payment_gateway.service.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vorchalov.payment_gateway.entity.ShopEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.ShopRepository;

import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public List<ShopEntity> getShopsForMerchant(UserEntity merchant) {
        return shopRepository.findAllByMerchant(merchant);
    }

    public List<ShopEntity> getAllShops() {
        return shopRepository.findAll();
    }

    public ShopEntity createShop(ShopEntity shop, UserEntity merchant) {
        shop.setMerchant(merchant);
        return shopRepository.save(shop);
    }

    @Transactional
    public ShopEntity updateShop(Long shopId, ShopEntity updated, UserEntity merchant) {
        ShopEntity existing = shopRepository.findByShopIdAndMerchant(shopId, merchant)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found or not yours"));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return existing;
    }

    public void deleteShop(Long shopId, UserEntity merchant) {
        ShopEntity existing = shopRepository.findByShopIdAndMerchant(shopId, merchant)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found or not yours"));

        shopRepository.delete(existing);
    }
}
