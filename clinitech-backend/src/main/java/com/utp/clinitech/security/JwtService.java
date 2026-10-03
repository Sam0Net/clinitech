package com.utp.clinitech.security;

import java.time.Instant; // Usado para registrar la marca temporal exacta de emisión y expiración en UTC.
import java.time.temporal.ChronoUnit; // Usado para sumar los minutos de vigencia al tiempo actual.
import java.util.Date; // Usado para compatibilidad con la API de generación de fechas de JJWT.
import javax.crypto.SecretKey; // Usado para representar la clave secreta criptográfica HMAC.
import org.springframework.beans.factory.annotation.Value; // Usado para inyectar el secreto y minutos de expiración de JWT.
import org.springframework.stereotype.Service; // Usado para marcar la clase como componente de servicio de seguridad en Spring.
import com.utp.clinitech.model.Usuario; // Usado para extraer el nombre de usuario y rol para los claims del token.
import io.jsonwebtoken.Claims; // Usado para leer y validar los atributos o claims contenidos dentro del token.
import io.jsonwebtoken.Jwts; // Usado como interfaz fluent builder para generar y parsear tokens JWT.
import io.jsonwebtoken.io.Decoders; // Usado para decodificar la clave secreta en formato Base64.
import io.jsonwebtoken.security.Keys; // Usado para generar la clave criptográfica segura a partir de los bytes decodificados.

// Servicio responsable de la generación, firma criptográfica y validación de tokens JWT.
@Service
public class JwtService {
  private final String secret; // Clave secreta en Base64 configurada en application.properties.
  private final long expirationMinutes; // Minutos de vigencia del token antes de expirar.

  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
    this.secret = secret; 
    this.expirationMinutes = expirationMinutes;
  }

  // Genera un token JWT firmado con el nombre de usuario y rol como claims.
  public String generar(Usuario usuario) {
    Instant now = Instant.now();
    return Jwts.builder().subject(usuario.getUsername()).claim("rol", usuario.getRol().name()).issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES))).signWith(key()).compact();
  }

  // Valida la firma del token y extrae sus claims (payload).
  public Claims validar(String token) { 
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload(); 
  }

  // Retorna la duración configurada de los tokens en minutos.
  public long expirationMinutes() { 
    return expirationMinutes; 
  }

  // Decodifica la clave secreta en Base64 y genera la clave HMAC-SHA para la firma.
  private SecretKey key() {
    byte[] bytes = Decoders.BASE64.decode(secret);
    if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
    return Keys.hmacShaKeyFor(bytes);
  }
}
