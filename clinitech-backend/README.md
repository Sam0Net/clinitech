# CliniTech backend

API REST para el diseño de CliniTech: autenticación, agenda, historial clínico, médicos, especialidades y reportes.

## Ejecutar

Necesitas Java 21 y PostgreSQL (o perfil de pruebas con H2). Define las variables antes de iniciar:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/clinitech'
export DB_USERNAME='clinitech'
export DB_PASSWORD='una-clave-de-base-de-datos'
export JWT_SECRET="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

### Cuentas Semilla Predeterminadas (Contraseña común: `CliniTech2026!`)
* **Administrador**: `admin`
* **Médico**: `medico` (Dr. Carlos Mendoza Ramos)
* **Paciente**: `paciente` (Roberto Gómez Salas)
* **Recepcionista**: `recepcion`

## Contrato principal para el frontend y evaluación

Rutas públicas: `POST /api/auth/login` y `POST /api/auth/registro`. Las demás requieren `Authorization: Bearer <accessToken>`.

| Módulo | Rutas |
| --- | --- |
| **Sesión** | `POST /api/auth/login`, `POST /api/auth/registro`, `GET /api/auth/me` |
| **Agenda y Citas** | `GET /api/citas`, `POST /api/citas`, `GET /api/citas/disponibilidad?medicoId=…&fecha=YYYY-MM-DD`, `PATCH /api/citas/{id}/reprogramar`, `PATCH /api/citas/{id}/estado` |
| **Triaje / Cola de Prioridad (Heap)** | `GET /api/citas/cola-atencion?medicoId=…&fecha=YYYY-MM-DD`, `GET /api/citas/cola-atencion/siguiente?medicoId=…` |
| **Historia Clínica** | `GET /api/consultas?pacienteId=…`, `POST /api/consultas` |
| **Pacientes y Árbol Binario (ABB)** | CRUD `/api/pacientes`, `GET /api/pacientes/arbol/buscar/{dni}`, `GET /api/pacientes/arbol/en-orden` |
| **Especialidades y Grafo (BFS)** | `GET /api/especialidades`, `POST /api/especialidades`, `GET /api/especialidades/{id}/relacionadas`, `GET /api/especialidades/interconsultas/ruta?origenId=…&destinoId=…` |
| **Médicos** | `GET /api/medicos?page=0&size=20`, `GET /api/medicos?especialidadId=…`, CRUD `/api/medicos` |
| **Reportes** | `GET /api/reportes/administrativo?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` |

El login acepta roles flexibles tanto en español como en inglés (`"patient"`, `"doctor"`, `"admin"`, `"PACIENTE"`, `"MEDICO"`, `"ADMIN"`).

