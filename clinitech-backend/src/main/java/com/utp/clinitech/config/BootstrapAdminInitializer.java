package com.utp.clinitech.config;

import org.springframework.beans.factory.annotation.Value; // Usado para inyectar propiedades de entorno y configuraciones de arranque.
import org.springframework.boot.ApplicationArguments; // Usado para recibir los argumentos pasados en la ejecución de la aplicación.
import org.springframework.boot.ApplicationRunner; // Usado para ejecutar lógica de arranque automático tras iniciar el contexto.
import org.springframework.security.crypto.password.PasswordEncoder; // Usado para codificar contraseñas con hashing criptográfico seguro.
import org.springframework.stereotype.Component; // Usado para registrar la clase como un componente bean gestionado por Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para garantizar la persistencia atómica del usuario administrador.
import com.utp.clinitech.dao.UsuarioDAO; // Usado para verificar existencia y persistir el usuario administrador inicial.
import com.utp.clinitech.model.Usuario; // Usado para instanciar la entidad del usuario administrador.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para asignar el rol ADMIN a la cuenta creada.

/** Crea el primer administrador sólo cuando se configuran variables de entorno explícitas. */
@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
  private final UsuarioDAO usuarios; 
  private final PasswordEncoder passwordEncoder;

  @Value("${app.bootstrap.admin.username:}") 
  private String username; // Nombre de usuario parametrizado.

  @Value("${app.bootstrap.admin.password:}") 
  private String password; // Contraseña en texto plano para encriptar.

  // Constructor con inyección de dependencias de persistencia y encriptación.
  public BootstrapAdminInitializer(UsuarioDAO usuarios, PasswordEncoder passwordEncoder) { 
    this.usuarios = usuarios; 
    this.passwordEncoder = passwordEncoder; 
  }

  // Ejecuta la verificación y creación del usuario de arranque si la base de datos está vacía.
  @Override 
  @Transactional
  public void run(ApplicationArguments args) {
    if (usuarios.count() != 0 || username.isBlank()) return; // Evita duplicar si ya existen usuarios o no se definió variable.
    if (password.length() < 12) throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD debe tener al menos 12 caracteres");
    usuarios.save(new Usuario(username.trim(), passwordEncoder.encode(password), RolUsuario.ADMIN, null, null));
  }
}
