package ru.vorchalov.payment_gateway.service.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BankEmulatorService {

    private final RestTemplate restTemplate;
    private final String bankEmulatorUrl;

    public BankEmulatorService(@Value("${bank.emulator.url}") String bankEmulatorUrl) {
        this.restTemplate = new RestTemplate();
        this.bankEmulatorUrl = bankEmulatorUrl;
    }

    public String getResponseCode(String cardNumber) {
        String url = bankEmulatorUrl + "?cardNumber=" + cardNumber;
        return restTemplate.getForObject(url, String.class);
    }
}
