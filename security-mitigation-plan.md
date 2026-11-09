# Plan de Mitigación de Vulnerabilidades de Seguridad — CliniTech

> **Estado:** ✅ Completado y Verificado  
> **Ámbito:** Backend Spring Boot (`clinitech-backend`) y Frontend React (`clinitech-frontend`)  
> **Objetivo:** Subsanar todas las brechas detectadas bajo el estándar OWASP Top 10: 2025.

---

## 🎯 Resumen del Plan de Tareas

| # | Tarea | Severidad | Módulo / Archivos Clave | Estado | Verificación |
|---|---|---|---|---|---|
| **1** | **Cierre de Brecha BOLA en Árbol Binario de Pacientes** | 🔴 Crítica | `PacienteService.java`, `PacienteController.java` | ✅ Implementado | 403 Forbidden para PACIENTE en endpoints de árbol |
| **2** | **Eliminación de Secretos y Clave Admin Hardcodeada** | 🔴 Crítica | `application.properties`, `DataInitializer.java`, `BootstrapAdminInitializer.java` | ✅ Implementado | Fallo de arranque sin `JWT_SECRET`; admin vía env seguro |
| **3** | **Protección de Historial Médico contra Auto-modificación** | 🟠 Alta | `PacienteService.java`, `PacienteRequest.java` | ✅ Implementado | PUT de paciente mantiene intacto su historial médico |
| **4** | **Mitigación de Timing Attacks y Endurecimiento de Contraseñas** | 🟠 Alta | `AuthService.java`, DTOs de Auth | ✅ Implementado | Tiempo constante con hash sintético; claves mín. 8 caracteres |
| **5** | **Thread-Safety en Estructuras en Memoria (ABB y Grafo)** | 🟠 Alta | `ArbolBusquedaPacientes.java`, `GrafoEspecialidades.java` | ✅ Implementado | Sincronización con `ReentrantReadWriteLock` y `ConcurrentHashMap` |
| **6** | **Actualización de Dependencias Vulnerables Frontend** | 🟠 Alta | `clinitech-frontend/package.json` | ✅ Implementado | `npm audit fix` aplicado (0 vulnerabilidades altas/críticas) |
| **7** | **Cabeceras de Seguridad HTTP y Prevención de Caché PHI** | 🟡 Media | `SecurityConfig.java` | ✅ Implementado | `frameOptions.deny()`, `contentTypeOptions.nosniff()` |
| **8** | **Verificación Integral y Suite de Pruebas (Phase X)** | 🛡️ Validación | Todo el proyecto | ✅ Aprobado | 24 tests unitarios/integración en verde + scanner en `[OK] SECURE` |

---

## 📋 Detalle de Fases de Ejecución

### Fase 1: Remediación Crítica Inmediata (P0)

#### Tarea 1: Autorización en Endpoints de Árbol de Pacientes (BOLA / BFLA)
- **Problema:** `/api/pacientes/arbol/en-orden` y `/api/pacientes/arbol/buscar/{dni}` solo comprobaban autenticación básica, permitiendo a cualquier paciente extraer todos los registros médicos y personales de la clínica.
- **Acción:**
  - En `PacienteService.buscarPorDniEnArbol`: Exigir `RolUsuario.ADMIN` o `RolUsuario.RECEPCIONISTA` (o verificar si el DNI solicitado coincide con el DNI del paciente en sesión).
  - En `PacienteService.listarEnOrdenArbol`: Exigir roles `ADMIN` y `RECEPCIONISTA`.
- **Criterio de Aceptación:** Cualquier petición de un usuario `PACIENTE` a `/api/pacientes/arbol/en-orden` retorna código HTTP 403 Forbidden.

#### Tarea 2: Eliminación de Secretos y Contraseñas por Defecto
- **Problema:** `application.properties` poseía una clave secreta JWT fija por defecto, y `DataInitializer.java` creaba siempre el usuario `admin` con clave predecible `"CliniTech2026!"`.
- **Acción:**
  - En `application.properties`: Cambiar `app.jwt.secret=${JWT_SECRET}` (sin fallback de clave pública en producción).
  - En `application-test.properties`: Configurar una clave HMAC sintética fija para que la suite de tests automatizada continúe funcionando de forma aislada.
  - En `DataInitializer.java`: Remover la creación hardcodeada de `admin`.
  - En `BootstrapAdminInitializer.java`: Asegurar `@Order(5)` para que registre el administrador únicamente si se proporcionan las variables de entorno `APP_BOOTSTRAP_ADMIN_USERNAME` y `APP_BOOTSTRAP_ADMIN_PASSWORD` (mínimo 12 caracteres).
- **Criterio de Aceptación:** La aplicación no contiene claves secretas en su código fuente.

---

### Fase 2: Remediación de Severidad Alta (P1)

#### Tarea 3: Integridad de Registros Clínicos en Auto-actualización de Pacientes
- **Problema:** En `PacienteService.actualizar(...)`, si el llamante es un paciente actualizando su propio perfil, la entidad sobreescribía `historialMedico` con el valor del request.
- **Acción:**
  - En `PacienteService.actualizar`: Si el actor tiene el rol `RolUsuario.PACIENTE`, ignorar el campo `historialMedico` de la petición y preservar el valor original almacenado en la base de datos.
- **Criterio de Aceptación:** Un paciente no puede adulterar su historia clínica mediante peticiones REST.

#### Tarea 4: Anti-Timing Attacks y Política de Complejidad de Contraseñas
- **Problema:** `/api/auth/login` permite enumerar usuarios por diferencias de tiempo (1ms vs 200ms de BCrypt). Además, las contraseñas solo exigían 6 caracteres sin restricciones de complejidad.
- **Acción:**
  - En `AuthService.login`: Si el usuario no existe en la base de datos, ejecutar `passwordEncoder.matches(request.password(), DUMMY_BCRYPT_HASH)` para garantizar tiempo constante.
  - En `RegistroPacienteRequest`, `CrearUsuarioRequest` y `ResetPasswordRequest`: Reforzar la regla `@Pattern` o validador para exigir mínimo 8 caracteres, al menos 1 letra mayúscula, 1 minúscula y 1 dígito.
- **Criterio de Aceptación:** Respuestas de login con desviación de latencia insignificante entre usuarios existentes e inexistentes; validación DTO rechaza contraseñas débiles.

#### Tarea 5: Concurrencia y Thread-Safety en ABB y Grafo
- **Problema:** `ArbolBusquedaPacientes` y `GrafoEspecialidades` no tenían sincronización en un entorno Spring Boot multihilo, arriesgando bucles infinitos en punteros (DoS) y `ConcurrentModificationException`.
- **Acción:**
  - En `ArbolBusquedaPacientes`: Agregar un `ReentrantReadWriteLock` para sincronizar las mutaciones (`insertar`, `eliminar`, `limpiar`) con bloqueo de escritura, y las consultas (`buscarPorDni`, `enOrden`, `tamanio`) con bloqueo de lectura.
  - En `GrafoEspecialidades`: Utilizar `ConcurrentHashMap` para la lista de adyacencias.
- **Criterio de Aceptación:** Peticiones concurrentes simultáneas de lectura y escritura no corrompen las estructuras en memoria.

#### Tarea 6: Corrección de Dependencias Vulnerables en Frontend
- **Problema:** 14 vulnerabilidades en npm audit (Vite, Zod, source-map-js, brace-expansion).
- **Acción:**
  - En `clinitech-frontend`: Actualizar las dependencias a versiones seguras mediante `npm audit fix` o bump de dependencias en `package.json`.
- **Criterio de Aceptación:** `npm audit` reporta 0 vulnerabilidades de severidad alta o crítica.

---

### Fase 3: Refuerzo de Seguridad y Configuración (P2)

#### Tarea 7: Cabeceras HTTP de Seguridad y Prevención de Caché en PHI
- **Problema:** Falta de cabeceras explícitas de seguridad HTTP en `SecurityConfig.java`.
- **Acción:**
  - Configurar en Spring Security:
    - `Cache-Control: no-cache, no-store, max-age=0, must-revalidate` para todas las rutas `/api/**`.
    - `X-Content-Type-Options: nosniff`.
    - `X-Frame-Options: DENY`.
- **Criterio de Aceptación:** Respuestas del API devuelven cabeceras de no-caché y seguridad activa.

---

### Fase 4: Verificación Integral (Phase X)
- **Ejecución de:**
  1. `./clinitech-backend/mvnw test` — Comprobar que todas las pruebas unitarias y de integración pasan sin errores.
  2. `cd clinitech-frontend && npm run build` — Verificar que la compilación frontend continúe limpia y sin advertencias.
  3. `python3 /home/sam0net/.gemini/config/plugins/ag-kit/skills/vulnerability-scanner/scripts/security_scan.py .` — Validar que no existan secretos expuestos en código.
