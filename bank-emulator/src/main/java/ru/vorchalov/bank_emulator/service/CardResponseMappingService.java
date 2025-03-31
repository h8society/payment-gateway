package ru.vorchalov.bank_emulator.service;

import org.springframework.stereotype.Service;
import ru.vorchalov.bank_emulator.entity.CardResponseMapping;
import ru.vorchalov.bank_emulator.repository.CardResponseMappingRepository;

import java.util.Optional;

@Service
public class CardResponseMappingService {

    private final CardResponseMappingRepository repository;

    public CardResponseMappingService(CardResponseMappingRepository repository) {
        this.repository = repository;
    }

    public String getResponseCodeByCardNumber(String cardNumber) {
        CardResponseMapping mapping = repository.findByCardNumber(cardNumber);
        if (mapping == null) {
            return "DECLINED";
        }
        return mapping.getResponseCode();
    }

    public CardResponseMapping createMapping(String cardNumber, String responseCode, String description) {
        CardResponseMapping mapping = new CardResponseMapping(cardNumber, responseCode, description);
        return repository.save(mapping);
    }

    public Optional<CardResponseMapping> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteMapping(Long id) {
        repository.deleteById(id);
    }

    public boolean updateResponseCode(String cardNumber, String newCode) {
        CardResponseMapping mapping = repository.findByCardNumber(cardNumber);
        if (mapping == null) {
            return false;
        }
        mapping.setResponseCode(newCode);
        repository.save(mapping);
        return true;
    }
}
