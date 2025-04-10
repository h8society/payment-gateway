package ru.vorchalov.payment_gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.vorchalov.payment_gateway.entity.ShopEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.ShopRepository;
import ru.vorchalov.payment_gateway.service.shop.ShopService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;

    private UserEntity merchant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        merchant = new UserEntity();
        merchant.setUserId(42L);
        merchant.setUsername("test_merchant");
    }

    @Test
    void getShopsForMerchant_returnsList() {
        ShopEntity shop = new ShopEntity();
        shop.setName("Test Shop");

        when(shopRepository.findAllByMerchant(merchant)).thenReturn(List.of(shop));

        List<ShopEntity> result = shopService.getShopsForMerchant(merchant);

        assertEquals(1, result.size());
        assertEquals("Test Shop", result.get(0).getName());
    }

    @Test
    void getAllShops_returnsAll() {
        when(shopRepository.findAll()).thenReturn(List.of(new ShopEntity(), new ShopEntity()));

        List<ShopEntity> result = shopService.getAllShops();

        assertEquals(2, result.size());
    }

    @Test
    void createShop_setsMerchantAndSaves() {
        ShopEntity input = new ShopEntity();
        input.setName("New Shop");

        when(shopRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ShopEntity result = shopService.createShop(input, merchant);

        assertEquals("New Shop", result.getName());
        assertEquals(merchant, result.getMerchant());
    }

    @Test
    void updateShop_successfullyUpdates() {
        ShopEntity existing = new ShopEntity();
        existing.setShopId(1L);
        existing.setName("Old Name");
        existing.setDescription("Old Desc");

        ShopEntity updated = new ShopEntity();
        updated.setName("New Name");
        updated.setDescription("New Desc");

        when(shopRepository.findByShopIdAndMerchant(1L, merchant)).thenReturn(Optional.of(existing));

        ShopEntity result = shopService.updateShop(1L, updated, merchant);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void updateShop_notFound_throwsException() {
        when(shopRepository.findByShopIdAndMerchant(99L, merchant)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shopService.updateShop(99L, new ShopEntity(), merchant)
        );

        assertTrue(exception.getMessage().contains("Shop not found"));
    }

    @Test
    void deleteShop_deletesSuccessfully() {
        ShopEntity shop = new ShopEntity();
        shop.setShopId(1L);
        when(shopRepository.findByShopIdAndMerchant(1L, merchant)).thenReturn(Optional.of(shop));

        shopService.deleteShop(1L, merchant);

        verify(shopRepository, times(1)).delete(shop);
    }

    @Test
    void deleteShop_notFound_throwsException() {
        when(shopRepository.findByShopIdAndMerchant(404L, merchant)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> shopService.deleteShop(404L, merchant));
    }
}
