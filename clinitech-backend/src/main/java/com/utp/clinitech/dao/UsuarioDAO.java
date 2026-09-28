package com.utp.clinitech.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.clinitech.model.Usuario;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByUsernameIgnoreCase(String username);

  boolean existsByUsernameIgnoreCase(String username);
}
