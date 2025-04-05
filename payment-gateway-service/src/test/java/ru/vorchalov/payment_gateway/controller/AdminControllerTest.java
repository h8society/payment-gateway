package ru.vorchalov.payment_gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import ru.vorchalov.payment_gateway.dto.AdminUserDto;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.dto.UpdateSettingRequest;
import ru.vorchalov.payment_gateway.dto.UpdateUserStatusRequest;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;
import ru.vorchalov.payment_gateway.entity.RoleEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private GatewaySettingRepository settingRepository;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getAllUsers → 200 OK with user list")
    void getAllUsers_ok() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("MERCHANT");

        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUsername("merchant1");
        user.setEmail("merchant@example.com");
        user.setHashedPassword("test");
        user.setRoles(Set.of(role));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        when(userRepository.findAll()).thenReturn(List.of(user));

        ResponseEntity<?> response = adminController.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        List<AdminUserDto> users = (List<AdminUserDto>) response.getBody();
        assertNotNull(users);
        assertEquals(1, users.size());

        AdminUserDto dto = users.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("merchant1", dto.getUsername());
        assertEquals("merchant@example.com", dto.getEmail());
        assertTrue(dto.isActive());
        assertEquals(List.of("MERCHANT"), dto.getRoles());
    }

    @Test
    @DisplayName("getAllTransactions → 200 OK with transaction list")
    void getAllTransactions_ok() {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setTransactionId(1L);
        dto.setAmount(new BigDecimal("99.99"));
        dto.setStatusCode("paid");
        dto.setResponseCode("00");
        dto.setTransactionDate(LocalDateTime.of(2024, 1, 1, 12, 0));
        dto.setBinBrand("VISA");
        dto.setBinBankName("Test Bank");
        dto.setBinCountry("Germany");

        when(paymentTransactionService.getAllTransactions()).thenReturn(List.of(dto));

        ResponseEntity<?> response = adminController.getAllTransactions();

        assertEquals(200, response.getStatusCodeValue());
        List<PaymentTransactionDto> list = (List<PaymentTransactionDto>) response.getBody();
        assertNotNull(list);
        assertEquals(1, list.size());

        PaymentTransactionDto returned = list.get(0);
        assertEquals(1L, returned.getTransactionId());
        assertEquals(new BigDecimal("99.99"), returned.getAmount());
        assertEquals("paid", returned.getStatusCode());
        assertEquals("00", returned.getResponseCode());
        assertEquals("VISA", returned.getBinBrand());
        assertEquals("Test Bank", returned.getBinBankName());
        assertEquals("Germany", returned.getBinCountry());
    }

    @Test
    @DisplayName("updateUserStatus → 204 No Content when updated")
    void updateUserStatus_ok() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUsername("merchant1");
        user.setEmail("merchant@example.com");
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserStatusRequest req = new UpdateUserStatusRequest();
        req.setActive(false);

        ResponseEntity<Void> response = adminController.updateUserStatus(1L, req);

        assertEquals(204, response.getStatusCodeValue());
        assertFalse(user.isActive());
    }

    @Test
    @DisplayName("updateSetting → 204 No Content when updated")
    void updateSetting_ok() {
        GatewaySettingEntity setting = new GatewaySettingEntity();
        setting.setKey("PAYMENT_TTL_MINUTES");
        setting.setValue("15");

        when(settingRepository.findById("PAYMENT_TTL_MINUTES"))
                .thenReturn(Optional.of(setting));
        when(settingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSettingRequest req = new UpdateSettingRequest();
        req.setKey("PAYMENT_TTL_MINUTES");
        req.setValue("30");

        ResponseEntity<Void> response = adminController.updateSetting(req);

        assertEquals(204, response.getStatusCodeValue());
        assertEquals("30", setting.getValue());
    }
}
