package ru.vorchalov.bank_emulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vorchalov.bank_emulator.entity.CardResponseMapping;
import ru.vorchalov.bank_emulator.repository.CardResponseMappingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardResponseMappingServiceTest {

    private CardResponseMappingRepository repository;
    private CardResponseMappingService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CardResponseMappingRepository.class);
        service = new CardResponseMappingService(repository);
    }

    @Test
    void testGetResponseCodeByCardNumber_WhenExists() {
        CardResponseMapping mapping = new CardResponseMapping("1234567890123456", "APPROVED", "Test");
        when(repository.findByCardNumber("1234567890123456")).thenReturn(mapping);

        String result = service.getResponseCodeByCardNumber("1234567890123456");

        assertEquals("APPROVED", result);
        verify(repository).findByCardNumber("1234567890123456");
    }

    @Test
    void testGetResponseCodeByCardNumber_WhenNotExists() {
        when(repository.findByCardNumber("9999999999999999")).thenReturn(null);

        String result = service.getResponseCodeByCardNumber("9999999999999999");

        assertEquals("DECLINED", result);
    }

    @Test
    void testCreateMapping() {
        CardResponseMapping input = new CardResponseMapping("1234567890123456", "DECLINED", "Тестовая карта");
        when(repository.save(any())).thenReturn(input);

        CardResponseMapping result = service.createMapping("1234567890123456", "DECLINED", "Тестовая карта");

        assertNotNull(result);
        assertEquals("DECLINED", result.getResponseCode());
        verify(repository).save(any());
    }

    @Test
    void testUpdateResponseCode_WhenCardExists() {
        CardResponseMapping existing = new CardResponseMapping("1234567890123456", "DECLINED", "init");
        when(repository.findByCardNumber("1234567890123456")).thenReturn(existing);
        when(repository.save(any())).thenReturn(existing);

        boolean updated = service.updateResponseCode("1234567890123456", "APPROVED");

        assertTrue(updated);
        assertEquals("APPROVED", existing.getResponseCode());
        verify(repository).save(existing);
    }

    @Test
    void testUpdateResponseCode_WhenCardNotExists() {
        when(repository.findByCardNumber("0000111122223333")).thenReturn(null);

        boolean updated = service.updateResponseCode("0000111122223333", "DECLINED");

        assertFalse(updated);
        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteMapping() {
        service.deleteMapping(10L);
        verify(repository).deleteById(10L);
    }

    @Test
    void testFindById() {
        CardResponseMapping mapping = new CardResponseMapping("1111222233334444", "APPROVED", "desc");
        when(repository.findById(5L)).thenReturn(Optional.of(mapping));

        Optional<CardResponseMapping> result = service.findById(5L);

        assertTrue(result.isPresent());
        assertEquals("APPROVED", result.get().getResponseCode());
    }
}
