package com.yefersoncm.simulador_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Esta es la clase de entrada principal para la aplicación Spring Boot.
 * Su único propósito es arrancar el framework.
 */
@SpringBootApplication
public class SimuladorApiApplication {

	public static void main(String[] args) {
		// Esta única línea arranca toda la aplicación Spring:
		// Carga la configuración, inicia el servidor web y activa tu API.
		SpringApplication.run(SimuladorApiApplication.class, args);
	}

}