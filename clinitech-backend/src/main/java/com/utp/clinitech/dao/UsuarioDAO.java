package com.utp.clinitech.dao;

import java.util.Optional; // Usado para encapsular el usuario encontrado de forma segura evitando nulls.
import org.springframework.data.jpa.repository.JpaRepository; // Usado para operaciones CRUD de persistencia sobre cuentas de usuario.
import com.utp.clinitech.model.Usuario; // Usado para representar la entidad Usuario en la base de datos.

// Repositorio de acceso a datos (DAO) para credenciales y cuentas de usuario.
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
  // Busca un usuario por su nombre de cuenta (username) ignorando mayúsculas y minúsculas.
  Optional<Usuario> findByUsernameIgnoreCase(String username);

  // Comprueba si ya existe un usuario registrado con el nombre de usuario indicado.
  boolean existsByUsernameIgnoreCase(String username);

  // Lista todos los usuarios ordenados por ID de forma descendente.
  java.util.List<Usuario> findAllByOrderByIdDesc();

  // Lista los usuarios filtrados por rol.
  java.util.List<Usuario> findByRolOrderByIdDesc(com.utp.clinitech.model.enums.RolUsuario rol);
}
