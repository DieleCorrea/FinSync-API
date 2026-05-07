package com.financas.tema1.DTO;

public class UserRegisterDTO {
    private String email;
    private String password;

    public UserRegisterDTO() {}

    public UserRegisterDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}