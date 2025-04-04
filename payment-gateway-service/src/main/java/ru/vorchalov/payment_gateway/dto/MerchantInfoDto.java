package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

@Data
public class MerchantInfoDto {
    private Long id;
    private String username;
    private String email;
}
