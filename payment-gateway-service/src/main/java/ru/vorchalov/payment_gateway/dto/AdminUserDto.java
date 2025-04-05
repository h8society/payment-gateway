package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private boolean active;
}
