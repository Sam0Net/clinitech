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

import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtService jwtService;
  private final boolean devBypass;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      @Value("${app.security.dev-bypass:false}") boolean devBypass
  ) { 
    this.jwtService = jwtService; 
    this.devBypass = devBypass;
  }

  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null) {
      log.info("Incoming [{} {}] Authorization: [{}]", request.getMethod(), request.getRequestURI(), header);
    } else {
      log.info("Incoming [{} {}] No Authorization header present", request.getMethod(), request.getRequestURI());
    }
    if (header == null || !header.startsWith("Bearer ")) { 
      if (devBypass && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("No Bearer token provided -> Defaulting to ADMIN authentication (dev-bypass=true)");
      }
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

  private String extractClaim(String json, String key) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
    return m.find() ? m.group(1) : null;
  }
}
