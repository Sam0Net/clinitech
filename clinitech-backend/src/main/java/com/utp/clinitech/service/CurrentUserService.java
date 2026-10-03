package com.utp.clinitech.service;

import org.springframework.http.HttpStatus; // Usado para asociar estados HTTP como 401 UNAUTHORIZED o 403 FORBIDDEN.
import org.springframework.security.core.Authentication; // Usado para leer el objeto de autenticación del contexto de seguridad.
import org.springframework.security.core.context.SecurityContextHolder; // Usado para acceder al contexto de seguridad del hilo actual.
import org.springframework.stereotype.Service; // Usado para declarar la clase como componente de servicio en Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para operaciones de lectura transaccionales de usuario.
import com.utp.clinitech.dao.UsuarioDAO; // Usado para buscar la entidad de usuario en base de datos según su username.
import com.utp.clinitech.exception.ApiException; // Usado para lanzar errores si no hay sesión o si la cuenta está inactiva.
import com.utp.clinitech.model.Usuario; // Usado para retornar el usuario autenticado con sus datos y roles.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para validar si el rol del usuario coincide con los requeridos.

// Servicio utilitario para recuperar el usuario actualmente autenticado y validar sus permisos.
@Service
public class CurrentUserService {
  private final UsuarioDAO usuarioDAO;

  public CurrentUserService(UsuarioDAO usuarioDAO) { 
    this.usuarioDAO = usuarioDAO; 
  }

  // Obtiene el usuario autenticado en la petición actual o lanza excepción 401 si no está autenticado.
  @Transactional(readOnly = true)
  public Usuario required() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) throw new ApiException(HttpStatus.UNAUTHORIZED, "Autenticación requerida");
    Usuario user = usuarioDAO.findByUsernameIgnoreCase(authentication.getName()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    if (!user.isActivo()) throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta está inactiva");
    return user;
  }

  // Valida que el usuario posea al menos uno de los roles permitidos para ejecutar la acción.
  public void requireRole(Usuario user, RolUsuario... roles) {
    for (RolUsuario role : roles) if (user.getRol() == role) return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
  }
}
