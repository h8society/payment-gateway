package ru.vorchalov.bank_emulator.repository;

import ru.vorchalov.bank_emulator.entity.CardResponseMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardResponseMappingRepository extends JpaRepository<CardResponseMapping, Long> {
    CardResponseMapping findByCardNumber(String cardNumber);
}
