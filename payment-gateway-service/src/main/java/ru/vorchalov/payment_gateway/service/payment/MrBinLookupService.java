package ru.vorchalov.payment_gateway.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.vorchalov.payment_gateway.dto.MrBinLookupResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class MrBinLookupService {

    private static final Logger log = LoggerFactory.getLogger(MrBinLookupService.class);

    private final RestTemplate restTemplate;
    private final String mrbinUrl;
    private final String mrbinAuthHeader;

    public MrBinLookupService(@Value("${mrbin.url}") String mrbinUrl) {
        this.restTemplate = new RestTemplate();
        this.mrbinUrl = mrbinUrl;
        this.mrbinAuthHeader = "Basic bXJiaW5pbzp0ZXN0X21yYmluaW8=";
    }

    public MrBinLookupResponse lookup(String bin) {
        log.info("🔍 [MrBin] Looking up info for BIN: {}", bin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", mrbinAuthHeader);

        Map<String, String> body = new HashMap<>();
        body.put("bin", bin);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            MrBinLookupResponse response = restTemplate.postForObject(mrbinUrl, requestEntity, MrBinLookupResponse.class);
            log.info("✅ [MrBin] BIN {} lookup result: {}", bin, response);
            return response;
        } catch (Exception ex) {
            log.error("❌ [MrBin] Error during BIN lookup: {}", ex.getMessage(), ex);
            return new MrBinLookupResponse();
        }
    }
}
