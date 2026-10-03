package com.utp.clinitech;

import org.springframework.boot.SpringApplication; // Usado para arrancar y configurar la aplicación Spring Boot.
import org.springframework.boot.autoconfigure.SpringBootApplication; // Usado para habilitar la autoconfiguración, escaneo de componentes y configuración de Spring Boot.

// Clase principal de punto de entrada del backend de CliniTech.
@SpringBootApplication
public class ClinitechBackendApplication {

	// Método principal que inicializa el contexto de Spring y arranca el servidor web embebido.
	public static void main(String[] args) {
		SpringApplication.run(ClinitechBackendApplication.class, args);
	}

}
