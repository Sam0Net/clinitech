package com.utp.clinitech.config;

import java.time.LocalDate; // Usado para registrar fechas de nacimiento de pacientes.
import java.time.OffsetDateTime; // Usado para registrar fechas y horas de citas de prueba con zona horaria.
import java.time.ZoneId; // Usado para configurar la zona horaria de Lima (America/Lima).
import java.util.List; // Usado para almacenar listas de citas en el guardado masivo.
import org.springframework.boot.ApplicationArguments; // Usado para recibir argumentos de arranque de la aplicación.
import org.springframework.boot.ApplicationRunner; // Usado para ejecutar automáticamente el sembrado de datos tras levantar el backend.
import org.springframework.core.annotation.Order; // Usado para definir la prioridad de ejecución frente a otros inicializadores.
import org.springframework.security.crypto.password.PasswordEncoder; // Usado para encriptar contraseñas de las cuentas semilla con BCrypt.
import org.springframework.stereotype.Component; // Usado para declarar la clase como un componente gestionado por Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para envolver la creación de datos de prueba en una transacción atómica.
import com.utp.clinitech.dao.CitaDAO; // Usado para verificar y persistir las citas médicas de prueba para la cola de triaje.
import com.utp.clinitech.dao.EspecialidadDAO; // Usado para verificar y sembrar las especialidades médicas.
import com.utp.clinitech.dao.MedicoDAO; // Usado para verificar y sembrar el personal médico de prueba.
import com.utp.clinitech.dao.PacienteDAO; // Usado para verificar y sembrar pacientes iniciales.
import com.utp.clinitech.dao.UsuarioDAO; // Usado para verificar y registrar los usuarios de prueba en el sistema.
import com.utp.clinitech.model.Cita; // Usado para instanciar las citas de demostración del Min-Heap.
import com.utp.clinitech.model.Especialidad; // Usado para instanciar especialidades médicas base.
import com.utp.clinitech.model.Medico; // Usado para instanciar doctores de prueba vinculados a especialidades.
import com.utp.clinitech.model.Paciente; // Usado para instanciar historias de pacientes de prueba.
import com.utp.clinitech.model.Usuario; // Usado para instanciar cuentas de login para admin, médico y paciente.
import com.utp.clinitech.model.enums.EstadoCita; // Usado para definir el estado inicial de las citas (PENDIENTE o CONFIRMADA).
import com.utp.clinitech.model.enums.PrioridadCita; // Usado para asignar niveles de urgencia (URGENTE o NORMAL) en el triaje.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para asignar los roles de seguridad a cada usuario semilla.

/**
 * Inicializador de datos semilla para demostración, pruebas y sustentación del proyecto.
 * Carga especialidades, médicos, pacientes y cuentas base con contraseñas conocidas.
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
    // 1. Especialidades clínicas iniciales (obtener o crear).
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

    // 2. Personal médico de demostración.
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

    // 3. Pacientes para indexar en el Árbol Binario de Búsqueda.
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

    // 4. Usuarios predeterminados con credenciales conocidas (Clave: CliniTech2026!).
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

    // 5. Citas de prueba para hoy para sustentar la Cola de Triaje (Min-Heap).
    if (citas.count() == 0) {
      OffsetDateTime hoy = OffsetDateTime.now(ZoneId.of("America/Lima"));

      // Creamos 5 citas con distintos niveles de urgencia y horarios
      // Cita 1: Condición NORMAL, hora 08:00 AM.
      Cita c1 = new Cita(pacGomez, drMendoza, hoy.withHour(8).withMinute(0), PrioridadCita.NORMAL, "Chequeo general");
      c1.cambiarEstado(EstadoCita.CONFIRMADA);

      // Cita 2: Condición NORMAL, hora 09:00 AM.
      Cita c2 = new Cita(pacFernandez, drMendoza, hoy.withHour(9).withMinute(0), PrioridadCita.NORMAL, "Dolor de cabeza leve");
      c2.cambiarEstado(EstadoCita.CONFIRMADA);

      // Cita 3: Condición URGENTE, hora 10:30 AM (el Heap la reordena a la cima).
      Cita c3 = new Cita(pacGomez, drMendoza, hoy.withHour(10).withMinute(30), PrioridadCita.URGENTE, "Fuerte dolor en el pecho");
      c3.cambiarEstado(EstadoCita.PENDIENTE);

      // Cita 4: Condición URGENTE, hora 11:00 AM.
      Cita c4 = new Cita(pacFernandez, drMendoza, hoy.withHour(11).withMinute(0), PrioridadCita.URGENTE, "Corte profundo en el brazo");
      c4.cambiarEstado(EstadoCita.PENDIENTE);

      // Cita 5: Condición NORMAL, hora 12:00 PM.
      Cita c5 = new Cita(pacGomez, drMendoza, hoy.withHour(12).withMinute(0), PrioridadCita.NORMAL, "Revisión de exámenes");
      c5.cambiarEstado(EstadoCita.PENDIENTE);

      citas.saveAll(List.of(c1, c2, c3, c4, c5));
    }
  }
}
