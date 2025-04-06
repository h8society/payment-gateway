package ru.vorchalov.payment_gateway.dto;

import lombok.Data;

@Data
public class MrBinLookupResponse {
    private String scheme;
    private String brand;
    private String country_name;
    private String bank_name;

    public String getCountry_name() {
        return country_name;
    }
    public void setCountry_name(String country_name) {
        this.country_name = country_name;
    }
    public String getBank_name() {
        return bank_name;
    }
    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }
}
