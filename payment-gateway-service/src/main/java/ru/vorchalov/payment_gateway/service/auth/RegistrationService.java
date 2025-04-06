package ru.vorchalov.payment_gateway.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vorchalov.payment_gateway.dto.RegistrationRequest;
import ru.vorchalov.payment_gateway.entity.*;
import ru.vorchalov.payment_gateway.repository.*;

import java.util.Set;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final MerchantKeyRepository keyRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepo,
                               RoleRepository roleRepo,
                               MerchantKeyRepository keyRepo,
                               BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.keyRepo = keyRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public MerchantKeyEntity generateApiKeyForUser(UserEntity user, String description) {
        MerchantKeyEntity key = new MerchantKeyEntity();
        key.setUser(user);
        key.setApiKey(UUID.randomUUID().toString());
        key.setDescription(description);
        key.setCreatedAt(java.time.LocalDateTime.now());
        key.setValidUntil(java.time.LocalDateTime.now().plusYears(1));

        return keyRepo.save(key);
    }

    public String registerUser(RegistrationRequest request) {
        RoleEntity role;

        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if(request.getRoleName() == null || request.getRoleName().isEmpty()) {
            role = roleRepo.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException(request.getRoleName() + " role not found"));
        } else {
            role = roleRepo.findByRoleName("MERCHANT")
                    .orElseThrow(() -> new RuntimeException("MERCHANT role not found"));
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        user = userRepo.save(user);

        MerchantKeyEntity key = generateApiKeyForUser(user, "Initial key");

        return key.getApiKey();
    }
}
