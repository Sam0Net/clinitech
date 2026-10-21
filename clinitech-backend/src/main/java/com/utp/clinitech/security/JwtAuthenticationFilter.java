package com.utp.clinitech.security;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Filtro interceptor HTTP que extrae, sanitiza y valida tokens Bearer JWT en cada petición.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) { 
    this.jwtService = jwtService; 
  }

  // Intercepta cada petición entrante y verifica el token de autenticación.
  @Override 
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header == null || !header.startsWith("Bearer ")) { 
      chain.doFilter(request, response); 
      return; 
    }

    String token = header.substring(7).trim();
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      token = token.substring(1, token.length() - 1).trim();
    }
    if (token.startsWith("<") && token.endsWith(">") && token.length() > 1) {
      token = token.substring(1, token.length() - 1).trim();
    }

    try {
      Claims claims = jwtService.validar(token);
      String username = claims.getSubject();
      String rol = claims.get("rol", String.class);
      if (username != null && rol != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Authentication SUCCESS for user: {} with role: {}", username, rol);
      }
    } catch (Exception e) { 
      log.warn("JWT validation failed: {}", e.getMessage());
      SecurityContextHolder.clearContext(); 
    }

    chain.doFilter(request, response);
  }
}
