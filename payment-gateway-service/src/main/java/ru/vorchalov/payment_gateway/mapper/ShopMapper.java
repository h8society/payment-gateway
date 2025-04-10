package ru.vorchalov.payment_gateway.mapper;

import ru.vorchalov.payment_gateway.dto.ShopDto;
import ru.vorchalov.payment_gateway.entity.ShopEntity;

public class ShopMapper {

    public static ShopDto toDto(ShopEntity entity) {
        ShopDto dto = new ShopDto();
        dto.setShopId(entity.getShopId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        if (entity.getMerchant() != null) {
            dto.setMerchantUsername(entity.getMerchant().getUsername());
        }
        return dto;
    }

    public static ShopEntity fromDto(ShopDto dto) {
        ShopEntity entity = new ShopEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
