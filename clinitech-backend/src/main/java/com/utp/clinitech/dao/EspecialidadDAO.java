package com.utp.clinitech.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.clinitech.model.Especialidad;

public interface EspecialidadDAO extends JpaRepository<Especialidad, Long> {
  List<Especialidad> findByActivoTrueOrderByNombreAsc();
}
