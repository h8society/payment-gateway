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

    public String registerMerchant(RegistrationRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        RoleEntity role = roleRepo.findByRoleName("MERCHANT")
                .orElseThrow(() -> new RuntimeException("MERCHANT role not found"));

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        user = userRepo.save(user);

        String apiKey = UUID.randomUUID().toString();
        MerchantKeyEntity key = new MerchantKeyEntity();
        key.setUser(user);
        key.setApiKey(apiKey);
        key.setDescription("Initial key");

        keyRepo.save(key);

        return apiKey;
    }
}
