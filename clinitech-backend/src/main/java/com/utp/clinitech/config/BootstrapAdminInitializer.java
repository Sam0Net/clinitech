package com.utp.clinitech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

/** Creates the first administrator only when explicit environment variables are supplied. */
@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
  private final UsuarioDAO usuarios; private final PasswordEncoder passwordEncoder;
  @Value("${app.bootstrap.admin.username:}") private String username;
  @Value("${app.bootstrap.admin.password:}") private String password;
  public BootstrapAdminInitializer(UsuarioDAO usuarios, PasswordEncoder passwordEncoder) { this.usuarios = usuarios; this.passwordEncoder = passwordEncoder; }
  @Override @Transactional
  public void run(ApplicationArguments args) {
    if (usuarios.count() != 0 || username.isBlank()) return;
    if (password.length() < 12) throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD debe tener al menos 12 caracteres");
    usuarios.save(new Usuario(username.trim(), passwordEncoder.encode(password), RolUsuario.ADMIN, null, null));
  }
}
