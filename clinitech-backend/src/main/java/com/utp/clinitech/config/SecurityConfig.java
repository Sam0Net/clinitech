package com.utp.clinitech.config;

import org.springframework.context.annotation.Bean; // Usado para registrar beans de seguridad en el contenedor de Spring.
import org.springframework.context.annotation.Configuration; // Usado para indicar que la clase contiene beans de configuración de seguridad.
import org.springframework.security.config.Customizer; // Usado para aplicar configuraciones predeterminadas (ej. CORS por defecto).
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Usado para configurar la seguridad a nivel de peticiones HTTP.
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Usado para habilitar el módulo de seguridad web de Spring Security.
import org.springframework.security.config.http.SessionCreationPolicy; // Usado para configurar la gestión sin estado (STATELESS) de las sesiones.
import org.springframework.security.core.userdetails.UserDetailsService; // Usado como servicio de búsqueda de usuarios en la autenticación.
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Usado para lanzar excepción si no se implementa autenticación por defecto de Spring.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Usado para implementar el cifrado seguro de contraseñas con el algoritmo BCrypt.
import org.springframework.security.crypto.password.PasswordEncoder; // Usado como interfaz estándar de codificación y verificación de contraseñas.
import org.springframework.security.web.SecurityFilterChain; // Usado para definir la cadena de filtros de seguridad HTTP de la aplicación.
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Usado como referencia para intercalar el filtro JWT en la cadena.
import com.utp.clinitech.security.JwtAuthenticationFilter; // Usado para registrar el filtro interceptor de tokens JWT personalizados.

// Configuración central de seguridad de la aplicación con Spring Security y JWT.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // Bean para codificar y verificar contraseñas usando BCrypt con sal automática.
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Servicio de usuarios deshabilitado a favor de la autenticación personalizada vía JWT.
  @Bean
  UserDetailsService userDetailsService() {
    return username -> {
      throw new UsernameNotFoundException("Autenticación por contraseña no disponible");
    };
  }

  // Define las reglas de autorización de rutas, deshabilitación de CSRF y filtro JWT.
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
    return http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults()) // Deshabilita CSRF para API REST sin estado y activa CORS.
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Modo STATELESS sin sesiones HTTP en servidor.
        .exceptionHandling(errors -> errors.authenticationEntryPoint(
            (request, response, exception) -> response.sendError(401, "Autenticación requerida"))) // Manejador de error para peticiones no autenticadas.
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login", "/api/auth/registro")
            .permitAll() // Endpoints públicos de login y registro de pacientes.
            .anyRequest().authenticated()) // Cualquier otra petición requiere autenticación.
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build(); // Registra el filtro JWT antes del filtro de usuario/contraseña estándar.
  }
}
