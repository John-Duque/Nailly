package com.nailly.infra.web.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados do perfil enviados ao editar a profissional. */
public class ProfessionalProfileRequest {

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String name;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    private String phone;

    @Size(max = 500, message = "A apresentação deve ter no máximo 500 caracteres.")
    private String bio;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
