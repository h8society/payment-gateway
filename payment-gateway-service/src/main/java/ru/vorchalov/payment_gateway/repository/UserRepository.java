package ru.vorchalov.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vorchalov.payment_gateway.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}
