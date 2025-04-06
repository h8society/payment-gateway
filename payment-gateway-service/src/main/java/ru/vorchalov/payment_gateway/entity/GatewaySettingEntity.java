package ru.vorchalov.payment_gateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gateway_settings")
public class GatewaySettingEntity {

    @Id
    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
