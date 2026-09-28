package com.utp.clinitech.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.utp.clinitech.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {
  private final String secret;
  private final long expirationMinutes;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
    this.secret = secret; this.expirationMinutes = expirationMinutes;
  }
  public String generar(Usuario usuario) {
    Instant now = Instant.now();
    return Jwts.builder().subject(usuario.getUsername()).claim("rol", usuario.getRol().name()).issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES))).signWith(key()).compact();
  }
  public Claims validar(String token) { return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload(); }
  public long expirationMinutes() { return expirationMinutes; }
  private SecretKey key() {
    byte[] bytes = Decoders.BASE64.decode(secret);
    if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
    return Keys.hmacShaKeyFor(bytes);
  }
}
