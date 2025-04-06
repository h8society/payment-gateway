package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

@Data
public class UpdateSettingRequest {
    private String key;
    private String value;
}
