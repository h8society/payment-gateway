package ru.vorchalov.payment_gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.vorchalov.payment_gateway.dto.ShopDto;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.MerchantKeyRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.shop.ShopService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShopController.class)
@WithMockUser(username = "merchant1", roles = {"MERCHANT"})
public class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShopService shopService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MerchantKeyRepository merchantKeyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/merchant/shops → 200 OK")
    void getShopsForMerchant() throws Exception {
        ShopDto dto = new ShopDto();
        dto.setShopId(1L);
        dto.setName("My Shop");
        dto.setDescription("Test Desc");

        UserEntity user = new UserEntity();
        user.setUsername("merchant1");

        when(userRepository.findByUsername("merchant1")).thenReturn(Optional.of(user));
        when(shopService.getShopsForMerchant(user)).thenReturn(List.of(dtoToEntity(dto, user)));

        mockMvc.perform(get("/api/merchant/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shopId", is(1)))
                .andExpect(jsonPath("$[0].name", is("My Shop")))
                .andExpect(jsonPath("$[0].description", is("Test Desc")));
    }

    @Test
    @DisplayName("POST /api/merchant/shops → 200 OK")
    void createShop() throws Exception {
        ShopDto dto = new ShopDto();
        dto.setName("New Shop");
        dto.setDescription("Nice shop");

        UserEntity user = new UserEntity();
        user.setUsername("merchant1");

        ShopDto savedDto = new ShopDto();
        savedDto.setShopId(10L);
        savedDto.setName(dto.getName());
        savedDto.setDescription(dto.getDescription());

        when(userRepository.findByUsername("merchant1")).thenReturn(Optional.of(user));
        when(shopService.createShop(any(), eq(user))).thenReturn(dtoToEntity(savedDto, user));

        mockMvc.perform(post("/api/merchant/shops")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId", is(10)))
                .andExpect(jsonPath("$.name", is("New Shop")))
                .andExpect(jsonPath("$.description", is("Nice shop")));
    }

    @Test
    @DisplayName("DELETE /api/merchant/shops/{id} → 204 No Content")
    void deleteShop() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("merchant1");

        when(userRepository.findByUsername("merchant1")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/merchant/shops/42").with(csrf()))
                .andExpect(status().isNoContent());

        verify(shopService).deleteShop(42L, user);
    }

    private ru.vorchalov.payment_gateway.entity.ShopEntity dtoToEntity(ShopDto dto, UserEntity merchant) {
        var entity = new ru.vorchalov.payment_gateway.entity.ShopEntity();
        entity.setShopId(dto.getShopId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setMerchant(merchant);
        return entity;
    }
}
