package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.RoleEntity;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleName(String roleName);
}
