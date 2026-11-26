package br.senac.almoxarifado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da API do almoxarifado da cantina escolar. */
@SpringBootApplication
public class AlmoxarifadoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlmoxarifadoApplication.class, args);
    }
}
