# PROYECTO ACADÉMICO: CLINITECH
**Curso:** Algoritmos y Estructuras de Datos  
**Ciclo:** 5to Ciclo  
**Institución:** Universidad Tecnológica del Perú (UTP)  

---

# INTRODUCCIÓN

Hoy en día, recibir una atención médica rápida y bien organizada en clínicas y centros de salud es fundamental para cualquier persona. Sin embargo, muchos centros médicos todavía tienen problemas cotidianos para administrar sus citas: se forman largas filas de espera, toma tiempo encontrar la información de los pacientes y no siempre se atiende primero a quien más lo necesita. En muchos lugares, la atención se da solo por orden de llegada, lo que ocasiona que personas con malestares urgentes tengan que esperar el mismo tiempo que alguien que solo asiste a un chequeo de rutina.

Para solucionar estos problemas, la tecnología y el desarrollo de software ofrecen alternativas prácticas y eficientes. Más allá del aspecto visual de una página web, lo más importante es cómo el sistema organiza y procesa la información internamente. El uso de estructuras de datos adecuadas permite que el sistema responda con rapidez: ayuda a ordenar la lista de espera dando prioridad a las urgencias médicas, permite buscar los datos de un paciente por su DNI en segundos y ayuda a conectar las distintas especialidades médicas para derivar a los pacientes de forma directa.

Con este propósito nace el proyecto **CliniTech**, un sistema web desarrollado para facilitar la gestión de citas médicas, el triaje y el control de historias clínicas. El objetivo principal de este trabajo es crear una plataforma fácil de usar que aplique estructuras de datos (como árboles binarios de búsqueda, colas de prioridad y grafos) para reducir los tiempos de espera y mejorar la atención de los pacientes.

Este proyecto busca beneficiar tanto a los pacientes, quienes recibirán una atención más oportuna según su estado de salud, como al personal médico y administrativo, que podrá organizar mejor su trabajo diario. A continuación, el documento presenta la descripción del problema, los objetivos generales y específicos, los alcances del sistema y las referencias bibliográficas que respaldan este trabajo.

---

# CAPÍTULO 1: GENERALIDADES DEL PROYECTO

## 1.1 Definición del Problema

En la actualidad, los establecimientos de atención médica ambulatoria (clínicas, policlínicos y centros de salud) enfrentan serios desafíos operativos en la coordinación eficiente de sus servicios de admisión, asignación de turnos, triaje y seguimiento de historiales clínicos. Con frecuencia, estos centros gestionan la programación de citas a través de métodos manuales, registros físicos o sistemas informáticos obsoletos que emplean estructuras de datos genéricas y no optimizadas (como listas simples o accesos secuenciales directos a disco sin indexación estratégica en memoria).

Esta arquitectura operativa genera cuellos de botella considerables:
1. **Tiempos de espera excesivos:** La asignación rígida y secuencial de turnos de atención no responde a la dinámica real de una consulta médica ambulatoria.
2. **Falta de priorización médica en sala de espera:** La atención administrada bajo un estricto orden de llegada (modelo FIFO tradicional) ignora el nivel de criticidad o gravedad del paciente, postergando casos con síntomas agudos o cuadros clínicos que requieren atención inmediata en favor de consultas de rutina.
3. **Sobrecarga de las consultas de búsqueda:** El incremento continuo en el número de pacientes registrados satura los mecanismos de búsqueda convencionales cuando se requiere acceder de forma inmediata al expediente durante la admisión o emergencia.
4. **Desconexión en el circuito de interconsultas:** La derivación de pacientes entre diversas especialidades médicas suele carecer de un modelado estructurado que permita identificar de forma ágil el trayecto clínico y las interconexiones asistenciales más cortas.

Como resultado, la experiencia del paciente se ve fuertemente deteriorada por prolongadas esperas e incertidumbre, mientras que el personal médico y administrativo padece sobrecarga laboral y falta de herramientas digitales que automaticen la toma de decisiones asistenciales cotidianas.

---

### 1.1.1 Descripción del Problema

Analizando a detalle la problemática identificada en los centros de atención clínica, se detectan cuatro dimensiones críticas que demandan una solución tecnológica soportada en estructuras de datos y algoritmos computacionales eficientes:

#### a) Carencia de un mecanismo dinámico y jerarquizado para el triaje y la cola de atención médica
En los sistemas de citas tradicionales, las atenciones del día se gestionan bajo una estructura de cola convencional (FIFO: *First-In, First-Out*). Si bien este esquema es justo para trámites puramente administrativos, resulta ineficiente e inseguro en el entorno de la salud:
* Un paciente clasificado como **URGENTE** (por ejemplo, con dolor torácico agudo, crisis hipertensiva o fiebre elevada persistente) debe esperar a que todos los pacientes con cita previa de prioridad **NORMAL** (chequeos de rutina, lectura de exámenes) sean atendidos primero si estos llegaron o reservaron antes.
* La reordenación manual de turnos por parte del personal de enfermería o recepción genera fricción entre usuarios, errores de coordinación y pérdida de trazabilidad de los horarios programados.
* La resolución de turnos requiere un modelo algorítmico capaz de extraer en tiempo mínimo al paciente de mayor urgencia y, en caso de empates de prioridad, resolver el orden en función del horario más temprano asignado.

#### b) Degradación en la recuperación de información y consulta de pacientes
El padrón de pacientes de un centro de salud crece de manera incremental y continua. Las consultas frecuentes de recepción exigen identificar al paciente a través de su Documento Nacional de Identidad (DNI):
* El empleo de búsquedas lineales sobre colecciones desordenadas o consultas repetitivas que dependan exclusivamente de barridos completos en la base de datos relacional generan una latencia no admisible frente a una alta concurrencia de solicitudes.
* No contar con un índice en memoria estructurado que permita búsquedas, inserciones y eliminaciones en tiempo promedio logarítmico $\mathcal{O}(\log n)$, además de imposibilitar la emisión de padrones ordenados alfabética o numéricamente de manera natural sin incurrir en algoritmos pesados de ordenamiento externo.

#### c) Fragmentación y lentitud en la derivación de interconsultas especializadas
Cuando un paciente acude a una consulta inicial (e.g. Medicina General) y se diagnostica una afección compleja, suele requerirse una interconsulta o derivación secuencial hacia especialidades afines (e.g. Cardiología, Nefrología, Cirugía Cardiovascular):
* La red de derivaciones entre especialidades médicas no se encuentra formalizada como un grafo de interconexión clínica, provocando derivaciones arbitrarias o desordenadas.
* Sin un algoritmo de exploración y cálculo de caminos mínimos, se pierde la capacidad de determinar la ruta de interconsulta óptima (menor número de escalas clínicas de referencia) para tratar al paciente de manera expedita.

#### d) Falta de integración, seguridad y trazabilidad operativa
* Los expedientes médicos y las anotaciones de las consultas suelen estar dispersos o no vinculados directamente a la cita que les dio origen.
* Falta de un sistema con control de acceso basado en roles (RBAC) que restrinja y proteja la confidencialidad de los datos médicos según el perfil del usuario (Administrador, Médico, Paciente).
* Ausencia de paneles de reportería que brinden a la administración métricas consolidadas sobre el flujo de citas (atendidas, pendientes, canceladas, reprogramadas) para la correcta toma de decisiones operativas.

---

## 1.2 Definición de Objetivos

### 1.2.1 Objetivo General

Desarrollar e implementar el sistema web integral **"CliniTech"** para la gestión de citas médicas, triaje de pacientes y administración de historiales clínicos, optimizando los tiempos de respuesta y la priorización de atención médica mediante el diseño e integración de estructuras de datos avanzadas (**Árboles Binarios de Búsqueda**, **Montículos Binarios** y **Grafos con Búsqueda en Anchura - BFS**) sobre una arquitectura desacoplada basada en Spring Boot 3 y React.

---

### 1.2.2 Objetivos Específicos

1. **Diseñar e implementar una Cola de Prioridad basada en un Montículo Binario (*Min-Heap*)** para el módulo de triaje y cola de atención médica del día, garantizando que los pacientes en estado **URGENTE** sean despachados prioritariamente frente a los de condición **NORMAL**, resolviendo empates por horario cronológico en tiempo logarítmico $\mathcal{O}(\log n)$ para inserción/extracción y $\mathcal{O}(1)$ para consulta del siguiente turno.
2. **Desarrollar un Árbol Binario de Búsqueda (ABB) indexado por DNI** para optimizar la gestión de pacientes en memoria, permitiendo inserciones ordenadas, eliminación de nodos con sustitución por sucesor *in-order*, cálculo de altura/tamaño y búsquedas directas en tiempo promedio $\mathcal{O}(\log n)$, así como recorridos *in-order* para la recuperación secuencial ordenada.
3. **Modelar la red de especialidades médicas e interconsultas a través de un Grafo no dirigido**, implementando el algoritmo de **Búsqueda en Anchura (*Breadth-First Search* - BFS)** para calcular y recomendar la ruta más corta (menor número de derivaciones intermedias) entre una especialidad de origen y una de destino.
4. **Construir una API REST modular y robusta en Java 21 con el framework Spring Boot 3**, aplicando una arquitectura en capas (Controladores, Servicios, DAOs y Entidades) con persistencia relacional en PostgreSQL e integridad transaccional (`@Transactional`).
5. **Implementar un sistema de seguridad y control de acceso basado en roles (RBAC) con Spring Security y JSON Web Tokens (JWT)**, garantizando el aislamiento de datos y protegiendo los privilegios diferenciados para Administradores, Médicos y Pacientes.
6. **Desarrollar una interfaz de usuario web moderna, interactiva y responsiva con React, Vite y Tailwind CSS**, facilitando a los pacientes la autogestión de citas y brindando a médicos y administradores paneles visuales para el triaje, la gestión de historias clínicas y el control de turnos.
7. **Diseñar un módulo de reportes y analítica administrativa**, permitiendo filtrar y consolidar métricas de desempeño asistencial (citas atendidas, canceladas, reprogramadas y carga de atención médica) en periodos de fechas definidos.
8. **Ejecutar pruebas unitarias automatizadas con JUnit 5** para validar de manera rigurosa la consistencia algorítmica, las propiedades estructurales y los casos de borde en el ABB, el Min-Heap y el Grafo de especialidades.

---

### 1.2.3 Alcances y Limitaciones

#### Alcances
El presente proyecto comprende el desarrollo de una solución informática integral orientada a la gestión ambulatoria de citas médicas, triaje asistencial e historiales clínicos. En el ámbito funcional y de seguridad, el sistema provee un esquema de autenticación y autorización basado en roles (Administrador, Médico y Paciente) mediante tokens JWT, permitiendo la programación de turnos en intervalos regulares de 30 minutos, la consulta de disponibilidad horaria y el registro estructurado de consultas médicas (diagnósticos y prescripciones).

En el ámbito algorítmico y de estructuras de datos, la solución abarca la implementación y optimización en memoria de tres estructuras fundamentales: un Montículo Binario (*Min-Heap*) para la gestión jerárquica de la cola de triaje según prioridad clínica y orden cronológico; un Árbol Binario de Búsqueda (ABB) indexado por DNI para acelerar la recuperación de pacientes en tiempo promedio $\mathcal{O}(\log n)$ y la emisión de padrones ordenados (*in-order*); y un Grafo no dirigido complementado con el algoritmo de Búsqueda en Anchura (BFS) para determinar la ruta óptima de derivación e interconsulta entre especialidades médicas. A nivel técnico, el alcance integra una API REST desacoplada desarrollada en Spring Boot 3 con persistencia relacional en PostgreSQL y una aplicación web interactiva desarrollada con React y Tailwind CSS, respaldada por un conjunto de pruebas unitarias automatizadas en JUnit 5.

#### Limitaciones
El desarrollo del proyecto se encuentra acotado por las siguientes delimitaciones contextuales, técnicas y operativas:
1. **Contexto asistencial:** El sistema está circunscrito a la atención ambulatoria en consultorios externos, prescindiendo del soporte para unidades de cuidados intensivos (UCI), áreas de shock trauma, hospitalización y gestión quirúrgica en tiempo real.
2. **Interoperabilidad externa:** No contempla la integración directa con servicios web de entidades gubernamentales (como el padrón nacional de RENIEC o sistemas del Ministerio de Salud / EsSalud), sustentando la validación de identidad en mecanismos locales de formato y unicidad en base de datos.
3. **Servicios financieros y de mensajería:** No se incorporan pasarelas de pago electrónico en línea ni servicios de mensajería masiva automatizada (SMS o WhatsApp Business API), concentrándose la interacción y confirmación de turnos exclusivamente en la plataforma web.
4. **Almacenamiento de archivos biomédicos:** El expediente clínico se restringe al registro de datos alfanuméricos estructurados, excluyendo el almacenamiento y procesamiento de imágenes diagnósticas de alta resolución en formato DICOM.
5. **Arquitectura de concurrencia y persistencia:** Las estructuras de datos en memoria (ABB y Grafo) operan de manera local dentro de una única instancia de la máquina virtual de Java (JVM), sin soporte para sincronización distribuida entre múltiples nodos o clústeres.

---

# REFERENCIAS BIBLIOGRÁFICAS

Banks, A., & Porcello, E. (2020). *Learning React: Modern patterns for developing React apps* (2nd ed.). O'Reilly Media.

Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2022). *Introduction to algorithms* (4th ed.). MIT Press.

Joyanes Aguilar, L., & Zahonero Martínez, I. (2008). *Estructuras de datos en Java*. McGraw-Hill Interamericana.

Ministerio de Salud del Perú. (2018). *Norma Técnica de Salud para la Gestión de la Historia Clínica* (NTS N.° 139-MINSA/2018/DGAIN; Resolución Ministerial N.° 214-2018/MINSA). MINSA. http://bvs.minsa.gob.pe/local/MINSA/4379.pdf

Organización Mundial de la Salud. (2020). *Estrategia mundial sobre salud digital 2020-2025*. Organización Mundial de la Salud. https://apps.who.int/iris/handle/10665/344249

Pressman, R. S., & Maxim, B. R. (2021). *Ingeniería del software: Un enfoque práctico* (9.ª ed.). McGraw-Hill Interamericana.

Sedgewick, R., & Wayne, K. (2011). *Algorithms* (4th ed.). Addison-Wesley Professional.

Walls, C. (2022). *Spring in action* (6th ed.). Manning Publications.
