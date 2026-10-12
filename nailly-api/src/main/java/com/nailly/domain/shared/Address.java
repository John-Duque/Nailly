package com.nailly.domain.shared;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Endereço brasileiro. UF com 2 letras maiúsculas e CEP no formato 00000-000. */
public final class Address {

    /** Tamanho da cópia do endereço guardada no agendamento (appointments.service_address). */
    public static final int MAX_TEXT_LENGTH = 400;

    private static final Pattern STATE_FORMAT = Pattern.compile("[A-Z]{2}");
    private static final Pattern ZIP_CODE = Pattern.compile("\\d{8}");

    private final String street;
    private final String number;
    private final String complement;
    private final String neighborhood;
    private final String city;
    private final String state;
    private final String zipCode;

    public Address(String street, String number, String complement, String neighborhood, String city,
                   String state, String zipCode) {
        this.street = DomainValidation.requireText(street, "Informe a rua.");
        this.number = DomainValidation.requireText(number, "Informe o número.");
        this.complement = DomainValidation.optionalText(complement);
        this.neighborhood = DomainValidation.requireText(neighborhood, "Informe o bairro.");
        this.city = DomainValidation.requireText(city, "Informe a cidade.");
        this.state = normalizeState(state);
        this.zipCode = normalizeZipCode(zipCode);
    }

    private static String normalizeState(String state) {
        String value = DomainValidation.requireText(state, "Informe a UF.").toUpperCase(Locale.ROOT);
        if (!STATE_FORMAT.matcher(value).matches()) {
            throw new DomainException("UF inválida.");
        }
        return value;
    }

    private static String normalizeZipCode(String zipCode) {
        String digits = DomainValidation.requireText(zipCode, "Informe o CEP.").replace("-", "");
        if (!ZIP_CODE.matcher(digits).matches()) {
            throw new DomainException("CEP inválido.");
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    /** Endereço em uma linha, como é copiado para o agendamento. */
    public String format() {
        String text = street + ", " + number + (complement == null ? "" : " - " + complement)
                + ", " + neighborhood + ", " + city + "/" + state + ", " + zipCode;
        return text.length() <= MAX_TEXT_LENGTH ? text : text.substring(0, MAX_TEXT_LENGTH);
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Address address && format().equals(address.format());
    }

    @Override
    public int hashCode() {
        return Objects.hash(format());
    }

    @Override
    public String toString() {
        return format();
    }
}
