package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantInfoDto {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> apiKeys;
}
