package ru.vorchalov.payment_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vorchalov.payment_gateway.dto.AdminUserDto;
import ru.vorchalov.payment_gateway.dto.PaymentTransactionDto;
import ru.vorchalov.payment_gateway.dto.UpdateSettingRequest;
import ru.vorchalov.payment_gateway.dto.UpdateUserStatusRequest;
import ru.vorchalov.payment_gateway.entity.GatewaySettingEntity;
import ru.vorchalov.payment_gateway.entity.UserEntity;
import ru.vorchalov.payment_gateway.repository.GatewaySettingRepository;
import ru.vorchalov.payment_gateway.repository.UserRepository;
import ru.vorchalov.payment_gateway.entity.RoleEntity;
import ru.vorchalov.payment_gateway.service.payment.PaymentTransactionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    private final PaymentTransactionService paymentTransactionService;

    private final GatewaySettingRepository settingRepository;

    public AdminController(UserRepository userRepository,
                           PaymentTransactionService paymentTransactionService,
                           GatewaySettingRepository settingRepository) {
        this.userRepository = userRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.settingRepository = settingRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        List<AdminUserDto> usersList =  userRepository.findAll().stream()
                .map(user -> {
                    AdminUserDto dto = new AdminUserDto();
                    dto.setId(user.getUserId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setActive(user.isActive());
                    dto.setRoles(user.getRoles().stream()
                            .map(RoleEntity::getRoleName)
                            .toList());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersList);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentTransactionDto>> getAllTransactions() {
        List<PaymentTransactionDto> transactions = paymentTransactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id,
                                                 @RequestBody UpdateUserStatusRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setActive(request.isActive());
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/settings")
    public ResponseEntity<Void> updateSetting(@RequestBody UpdateSettingRequest request) {
        GatewaySettingEntity setting = settingRepository.findById(request.getKey())
                .orElse(new GatewaySettingEntity());

        setting.setKey(request.getKey());
        setting.setValue(request.getValue());
        settingRepository.save(setting);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<List<GatewaySettingEntity>> getSettingsList() {
        List<GatewaySettingEntity> settings = settingRepository.findAll();

        return ResponseEntity.ok(settings);
    }
}

