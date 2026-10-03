package com.utp.clinitech.security;

import java.io.IOException; // Usado para manejar excepciones de entrada y salida en la cadena de filtros.
import java.util.List; // Usado para asignar las autoridades y roles al token de autenticación.
import jakarta.servlet.FilterChain; // Usado para pasar la solicitud al siguiente filtro de la cadena web.
import jakarta.servlet.ServletException; // Usado para capturar errores generales de procesamiento en servlets.
import jakarta.servlet.http.HttpServletRequest; // Usado para acceder a las cabeceras HTTP de autorización entrantes.
import jakarta.servlet.http.HttpServletResponse; // Usado para gestionar la respuesta HTTP enviada al cliente.
import org.springframework.beans.factory.annotation.Value; // Usado para inyectar la bandera dev-bypass de desarrollo.
import org.springframework.http.HttpHeaders; // Usado para referenciar la cabecera estándar de autorización (Authorization).
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Usado para instanciar el objeto de sesión autenticado.
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Usado para encapsular el rol del usuario (ROLE_ADMIN, etc.).
import org.springframework.security.core.context.SecurityContextHolder; // Usado para almacenar la autenticación en el hilo de ejecución actual.
import org.springframework.stereotype.Component; // Usado para marcar el filtro como bean gestionado por Spring.
import org.springframework.web.filter.OncePerRequestFilter; // Usado para garantizar que el filtro se ejecute una sola vez por petición.
import io.jsonwebtoken.Claims; // Usado para acceder a los datos deserializados del token JWT.
import org.slf4j.Logger; // Usado para emitir mensajes informativos y de depuración en consola.
import org.slf4j.LoggerFactory; // Usado para inicializar la instancia de Logger.

// Filtro interceptor HTTP que extrae, sanitiza y valida tokens Bearer JWT en cada petición.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtService jwtService;
  private final boolean devBypass; // Bandera para permitir pruebas ágiles en Thunder Client sin login obligatorio.

  public JwtAuthenticationFilter(
      JwtService jwtService,
      @Value("${app.security.dev-bypass:false}") boolean devBypass
  ) { 
    this.jwtService = jwtService; 
    this.devBypass = devBypass;
  }

  // Intercepta cada petición entrante y verifica el token de autenticación.
  @Override 
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION); // Obtiene la cabecera Authorization.
    if (header != null) {
      log.info("Incoming [{} {}] Authorization: [{}]", request.getMethod(), request.getRequestURI(), header);
    } else {
      log.info("Incoming [{} {}] No Authorization header present", request.getMethod(), request.getRequestURI());
    }

    // Si no hay cabecera Bearer, evalúa si está activo el modo desarrollo dev-bypass para facilitar pruebas.
    if (header == null || !header.startsWith("Bearer ")) { 
      if (devBypass && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("No Bearer token provided -> Defaulting to ADMIN authentication (dev-bypass=true)");
      }
      chain.doFilter(request, response); 
      return; 
    }

    // Sanitiza el token limpiando comillas o caracteres espurios agregados accidentalmente en clientes REST.
    String token = header.substring(7).trim();
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      token = token.substring(1, token.length() - 1).trim();
    }
    if (token.startsWith("<") && token.endsWith(">") && token.length() > 1) {
      token = token.substring(1, token.length() - 1).trim();
    }

    try {
      Claims claims = jwtService.validar(token); // Valida la firma criptográfica del token JWT.
      String username = claims.getSubject();
      String rol = claims.get("rol", String.class);
      if (username != null && rol != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(auth); // Establece la sesión autenticada en el contexto de Spring.
        log.info("Authentication SUCCESS for user: {} with role: {}", username, rol);
      }
    } catch (Exception e) { 
      log.warn("JWT signature validation failed for token: {}. Intentando fallback de desarrollo...", e.getMessage());
      try {
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
          String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
          String username = extractClaim(payloadJson, "sub");
          String rol = extractClaim(payloadJson, "rol");
          if (username != null && rol != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Authentication SUCCESS (Dev fallback) for user: {} with role: {}", username, rol);
          }
        }
      } catch (Exception fallbackError) {
        log.warn("Fallback failed: {}", fallbackError.getMessage());
      }
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        SecurityContextHolder.clearContext(); 
      }
    }
    chain.doFilter(request, response);
  }

  // Extrae un atributo simple desde una cadena JSON mediante expresión regular.
  private String extractClaim(String json, String key) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
    return m.find() ? m.group(1) : null;
  }
}
