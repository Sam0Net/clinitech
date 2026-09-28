package com.utp.clinitech.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

@Service
public class CurrentUserService {
  private final UsuarioDAO usuarioDAO;
  public CurrentUserService(UsuarioDAO usuarioDAO) { this.usuarioDAO = usuarioDAO; }
  @Transactional(readOnly = true)
  public Usuario required() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) throw new ApiException(HttpStatus.UNAUTHORIZED, "Autenticación requerida");
    Usuario user = usuarioDAO.findByUsernameIgnoreCase(authentication.getName()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    if (!user.isActivo()) throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta está inactiva");
    return user;
  }
  public void requireRole(Usuario user, RolUsuario... roles) {
    for (RolUsuario role : roles) if (user.getRol() == role) return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
  }
}
