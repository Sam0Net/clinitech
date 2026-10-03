package com.utp.clinitech.config;

import java.util.Arrays; // Usado para convertir arreglos de texto en colecciones de orígenes permitidos.
import java.util.List; // Usado para definir listas de métodos y cabeceras HTTP admitidas.
import org.springframework.beans.factory.annotation.Value; // Usado para inyectar los orígenes CORS permitidos desde application.properties.
import org.springframework.context.annotation.Bean; // Usado para declarar el método productor del bean de configuración CORS.
import org.springframework.context.annotation.Configuration; // Usado para marcar la clase como una clase de configuración de Spring.
import org.springframework.web.cors.CorsConfiguration; // Usado para definir las reglas de intercambio de recursos de origen cruzado (CORS).
import org.springframework.web.cors.CorsConfigurationSource; // Usado como contrato de origen de configuración CORS para Spring Security.
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Usado para asociar las reglas CORS a rutas URL de la API.

// Configuración de la política CORS para permitir la comunicación entre el frontend y el backend.
@Configuration
public class CorsConfig {

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins; // Orígenes cliente permitidos configurados en properties (ej. http://localhost:5173).

  // Define las cabeceras, métodos HTTP y credenciales habilitadas para las peticiones entrantes.
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList()); // Permite dominios específicos.
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Métodos HTTP habilitados.
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Cabeceras permitidas en las peticiones.
    configuration.setExposedHeaders(List.of("Location")); // Cabeceras visibles en las respuestas del navegador.
    configuration.setAllowCredentials(true); // Permite el envío de credenciales/cookies autenticadas.
    configuration.setMaxAge(3600L); // Tiempo de caché de la respuesta preflight en segundos.

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration); // Aplica las reglas CORS a todos los endpoints /api/**.
    return source;
  }
}
