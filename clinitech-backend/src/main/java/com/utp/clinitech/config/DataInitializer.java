package com.utp.clinitech.config;

import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import com.utp.clinitech.dao.CitaDAO;
import com.utp.clinitech.dao.EspecialidadDAO;
import com.utp.clinitech.dao.MedicoDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dao.UsuarioDAO;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.Especialidad;
import com.utp.clinitech.model.Medico;
import com.utp.clinitech.model.Paciente;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.EstadoCita;
import com.utp.clinitech.model.enums.PrioridadCita;
import com.utp.clinitech.model.enums.RolUsuario;

/**
 * Inicializador de datos semilla para demostración, pruebas y sustentación del
 * proyecto.
 * Carga especialidades, médicos, pacientes y cuentas base con contraseñas
 * conocidas.
 */
@Component
@Order(10)
public class DataInitializer implements ApplicationRunner {

  private final EspecialidadDAO especialidades;
  private final MedicoDAO medicos;
  private final PacienteDAO pacientes;
  private final UsuarioDAO usuarios;
  private final CitaDAO citas;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      EspecialidadDAO especialidades,
      MedicoDAO medicos,
      PacienteDAO pacientes,
      UsuarioDAO usuarios,
      CitaDAO citas,
      PasswordEncoder passwordEncoder) {
    this.especialidades = especialidades;
    this.medicos = medicos;
    this.pacientes = pacientes;
    this.usuarios = usuarios;
    this.citas = citas;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // 1. Especialidades clínicas (obtener o crear)
    Especialidad medGeneral = especialidades.findByActivoTrueOrderByNombreAsc().stream()
        .filter(e -> e.getNombre().equalsIgnoreCase("Medicina General"))
        .findFirst()
        .orElseGet(
            () -> especialidades.save(new Especialidad("Medicina General", "Atención primaria y triaje médico")));

    Especialidad cardiologia = especialidades.findByActivoTrueOrderByNombreAsc().stream()
        .filter(e -> e.getNombre().equalsIgnoreCase("Cardiología"))
        .findFirst()
        .orElseGet(() -> especialidades
            .save(new Especialidad("Cardiología", "Diagnóstico y tratamiento de afecciones cardíacas")));

    if (!especialidades.findByActivoTrueOrderByNombreAsc().stream()
        .anyMatch(e -> e.getNombre().equalsIgnoreCase("Pediatría"))) {
      especialidades.save(new Especialidad("Pediatría", "Atención médica integral infantil"));
      especialidades.save(new Especialidad("Dermatología", "Cuidado y tratamiento de la piel"));
      especialidades.save(new Especialidad("Traumatología", "Lesiones óseas y del sistema locomotor"));
    }

    // 2. Médicos
    Medico drMendoza;
    if (medicos.count() == 0) {
      drMendoza = new Medico();
      drMendoza.actualizar("Carlos", "Mendoza Ramos", "CMP45892", medGeneral, "987654321", "cmendoza@clinitech.com",
          "08:00 - 16:00");
      drMendoza = medicos.save(drMendoza);

      Medico draTorres = new Medico();
      draTorres.actualizar("Ana", "Torres Chávez", "CMP51234", cardiologia, "976543210", "atorres@clinitech.com",
          "09:00 - 17:00");
      medicos.save(draTorres);
    } else {
      drMendoza = medicos.findAll().get(0);
    }

    // 3. Pacientes
    Paciente pacGomez;
    Paciente pacFernandez;
    if (pacientes.count() == 0) {
      pacGomez = new Paciente();
      pacGomez.actualizar("72345678", "Roberto", "Gómez Salas", LocalDate.of(1995, 5, 14), "912345678",
          "rgomez@gmail.com", "Av. Arequipa 1234, Lima", "Penicilina", "Hipertensión leve");
      pacGomez = pacientes.save(pacGomez);

      pacFernandez = new Paciente();
      pacFernandez.actualizar("45678901", "Lucía", "Fernández Paz", LocalDate.of(1998, 11, 23), "923456789",
          "lfernandez@gmail.com", "Calle Los Pinos 450, Miraflores", "Ninguna", "Sin antecedentes");
      pacientes.save(pacFernandez);
    } else {
      pacGomez = pacientes.findAll().get(0);
      pacFernandez = pacientes.findAll().size() > 1 ? pacientes.findAll().get(1) : pacGomez;
    }

    // 4. Usuarios predeterminados (Clave: CliniTech2026!)
    String hashClave = passwordEncoder.encode("CliniTech2026!");

    if (!usuarios.existsByUsernameIgnoreCase("admin")) {
      usuarios.save(new Usuario("admin", hashClave, RolUsuario.ADMIN, null, null));
    }
    if (!usuarios.existsByUsernameIgnoreCase("medico")) {
      usuarios.save(new Usuario("medico", hashClave, RolUsuario.MEDICO, null, drMendoza));
    }
    if (!usuarios.existsByUsernameIgnoreCase("paciente")) {
      usuarios.save(new Usuario("paciente", hashClave, RolUsuario.PACIENTE, pacGomez, null));
    }
    if (!usuarios.existsByUsernameIgnoreCase("recepcion")) {
      usuarios.save(new Usuario("recepcion", hashClave, RolUsuario.RECEPCIONISTA, null, null));
    }

    // 5. Citas de prueba para hoy (Cola de Triaje)
    if (citas.count() == 0) {
      OffsetDateTime hoy = OffsetDateTime.now(ZoneId.of("America/Lima"));

      // Creamos 5 citas con distintos niveles de urgencia y horarios
      // 1. Cita normal, llegó temprano
      Cita c1 = new Cita(pacGomez, drMendoza, hoy.withHour(8).withMinute(0), PrioridadCita.NORMAL, "Chequeo general");
      c1.cambiarEstado(EstadoCita.CONFIRMADA);

      // 2. Cita normal, llegó después
      Cita c2 = new Cita(pacFernandez, drMendoza, hoy.withHour(9).withMinute(0), PrioridadCita.NORMAL,
          "Dolor de cabeza leve");
      c2.cambiarEstado(EstadoCita.CONFIRMADA);

      // 3. Cita URGENTE, llegó a media mañana (debería saltar a la cima)
      Cita c3 = new Cita(pacGomez, drMendoza, hoy.withHour(10).withMinute(30), PrioridadCita.URGENTE,
          "Fuerte dolor en el pecho");
      c3.cambiarEstado(EstadoCita.PENDIENTE);

      // 4. Otra URGENTE, llegó después de la primera urgente
      Cita c4 = new Cita(pacFernandez, drMendoza, hoy.withHour(11).withMinute(0), PrioridadCita.URGENTE,
          "Corte profundo en el brazo");
      c4.cambiarEstado(EstadoCita.PENDIENTE);

      // 5. Cita normal, al final
      Cita c5 = new Cita(pacGomez, drMendoza, hoy.withHour(12).withMinute(0), PrioridadCita.NORMAL,
          "Revisión de exámenes");
      c5.cambiarEstado(EstadoCita.PENDIENTE);

      citas.saveAll(List.of(c1, c2, c3, c4, c5));
    }
  }
}
