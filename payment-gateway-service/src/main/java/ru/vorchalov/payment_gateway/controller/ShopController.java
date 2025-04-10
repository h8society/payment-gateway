package ru.vorchalov.payment_gateway.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.ShopDto;
import ru.vorchalov.payment_gateway.entity.ShopEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.mapper.ShopMapper;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.shop.ShopService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ShopController {

    private final ShopService shopService;
    private final UserRepository userRepository;

    public ShopController(ShopService shopService, UserRepository userRepository) {
        this.shopService = shopService;
        this.userRepository = userRepository;
    }

    @GetMapping("/merchant/shops")
    public ResponseEntity<List<ShopDto>> getShopsForMerchant(Authentication auth) {
        UserEntity merchant = resolveUser(auth);
        List<ShopDto> result = shopService.getShopsForMerchant(merchant).stream()
                .map(ShopMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/shops")
    public ResponseEntity<List<ShopDto>> getAllShops() {
        List<ShopDto> result = shopService.getAllShops().stream()
                .map(ShopMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/merchant/shops")
    public ResponseEntity<ShopDto> createShop(@Valid @RequestBody ShopDto dto, Authentication auth) {
        UserEntity merchant = resolveUser(auth);
        ShopEntity shop = ShopMapper.fromDto(dto);
        ShopEntity created = shopService.createShop(shop, merchant);
        return ResponseEntity.ok(ShopMapper.toDto(created));
    }

    @PutMapping("/merchant/shops/{shopId}")
    public ResponseEntity<ShopDto> updateShop(@PathVariable Long shopId,
                                              @Valid @RequestBody ShopDto dto,
                                              Authentication auth) {
        UserEntity merchant = resolveUser(auth);
        ShopEntity updated = shopService.updateShop(shopId, ShopMapper.fromDto(dto), merchant);
        return ResponseEntity.ok(ShopMapper.toDto(updated));
    }

    @DeleteMapping("/merchant/shops/{shopId}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long shopId, Authentication auth) {
        UserEntity merchant = resolveUser(auth);
        shopService.deleteShop(shopId, merchant);
        return ResponseEntity.noContent().build();
    }

    private UserEntity resolveUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Пользователь не авторизован"
            );
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Пользователь не найден"
                ));
    }
}
