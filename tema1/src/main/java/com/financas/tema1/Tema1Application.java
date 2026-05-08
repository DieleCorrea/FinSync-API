package com.financas.tema1;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Tema1Application {

    public static void main(String[] args) {
        // Dotenv ANTES do Spring subir
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(Tema1Application.class, args);
    }
}
