package com.utp.clinitech.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.EspecialidadDAO;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.model.Especialidad;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

/**
 * Inicializador base: asegura el catálogo de especialidades y la cuenta de administrador inicial.
 * Los datos operativos (médicos, pacientes, citas) se crean exclusivamente desde el frontend.
 */
@Component
@Order(10)
public class DataInitializer implements ApplicationRunner {

  private final EspecialidadDAO especialidades;
  private final UsuarioDAO usuarios;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      EspecialidadDAO especialidades,
      UsuarioDAO usuarios,
      PasswordEncoder passwordEncoder) {
    this.especialidades = especialidades;
    this.usuarios = usuarios;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // 1. Especialidades clínicas iniciales (catálogo base)
    if (especialidades.count() == 0) {
      especialidades.save(new Especialidad("Medicina General", "Atención primaria y triaje médico"));
      especialidades.save(new Especialidad("Cardiología", "Diagnóstico y tratamiento de afecciones cardíacas"));
      especialidades.save(new Especialidad("Pediatría", "Atención médica integral infantil"));
      especialidades.save(new Especialidad("Dermatología", "Cuidado y tratamiento de la piel"));
      especialidades.save(new Especialidad("Traumatología", "Lesiones óseas y del sistema locomotor"));
    }

    // 2. Usuario administrador para el acceso inicial al sistema
    String hashClave = passwordEncoder.encode("CliniTech2026!");
    if (!usuarios.existsByUsernameIgnoreCase("admin")) {
      usuarios.save(new Usuario("admin", hashClave, RolUsuario.ADMIN, null, null));
    }
  }
}
