UniTask: Propuesta de Aplicación Móvil

"Tu asistente académico personal. Simple, rápido y enfocado en lo urgente."

Información del Proyecto

Autores: Muñiz Rivas Leopoldo Miquel

Tema: UniTask

Curso: 6 - A

Periodo: 2025 (2)

Contexto: Creación de Proyecto en Android (Ingeniería en Software)

1. El Problema

Muchos estudiantes universitarios, especialmente aquellos en semestres avanzados como el 6to, manejan una carga académica alta con múltiples asignaturas y fechas de entrega simultáneas.

El problema principal es la desorganización académica. Esto se manifiesta de varias formas:

Tareas descentralizadas: Las tareas se anotan en cuadernos, notas rápidas del teléfono, chats o, peor aún, se confían a la memoria.

Falta de priorización: Es difícil identificar qué tarea requiere atención inmediata, llevando a entregas de última hora o al olvido de trabajos importantes.

Herramientas inadecuadas: Las herramientas de gestión de proyectos (como Trello, Asana, etc.) son demasiado complejas para el ritmo estudiantil y sus funciones "extra" (equipos, pipelines, etc.) sobran. Las aplicaciones de "To-Do" genéricas no tienen el contexto de "asignaturas".

Necesidad Real: Los estudiantes necesitan una solución centralizada, minimalista y móvil, diseñada específicamente para registrar y visualizar tareas académicas de forma rápida y priorizada.

2. La Solución: UniTask

UniTask es una aplicación móvil para Android diseñada para ser la solución a esta desorganización.

Es una herramienta minimalista enfocada en tres pilares:

Registro Rápido: Añadir una tarea debe tomar menos de 10 segundos.

Contexto Académico: Todo gira en torno a las "Asignaturas".

Visualización de Urgencia: La app debe responder instantáneamente a la pregunta: "¿Qué tengo que entregar ya?"

UniTask elimina la complejidad y se enfoca en ser una libreta de tareas digital, inteligente y siempre disponible.

3. Público Objetivo

Usuario principal: Estudiantes universitarios y de instituto/colegio.

Contexto del usuario: Alguien que cursa múltiples asignaturas (3 a 7 materias) y necesita un método rápido para registrar fechas de exámenes, deberes, proyectos y exposiciones sin perder el enfoque.

4. Flujo de Pantallas y Funcionalidad

La aplicación se compondrá de tres pantallas principales para mantener la simplicidad.

Pantalla 1: Principal (Dashboard)

Esta es la primera pantalla que ve el usuario al abrir la app. Su propósito es mostrar la priorización.

Componente 1: Sección "Urgente"

Un carrusel o listado destacado en la parte superior.

Muestra solo las tareas con fecha de entrega en las próximas 24 a 48 horas.

Cada ítem muestra: Nombre de la tarea, Asignatura (con su color) y Hora de entrega.

Componente 2: Listado "Todas las Tareas"

Debajo de "Urgente", se listan todas las tareas pendientes.

Están ordenadas cronológicamente por fecha de entrega (de la más cercana a la más lejana).

Al completar una tarea (ej. con un checkbox), esta desaparece de la lista.

Componente 3: Botón Flotante (FAB)

Un botón "+" siempre visible en la esquina inferior derecha para navegar a la pantalla "Añadir Tarea".

[Imagen de un wireframe de app de tareas con sección urgente y lista general]

Pantalla 2: Añadir Tarea

Un formulario simple y directo para minimizar la fricción de entrada.

Campo 1: "Nombre de la Tarea" (Texto)

Ej: "Ensayo sobre Arquitectura Limpia".

Campo 2: "Asignatura" (Selector/Spinner)

El usuario selecciona de la lista de asignaturas que ya ha registrado (ej. "Arquitectura de Software", "Sistemas Operativos").

Debe incluir una opción rápida para "+ Añadir nueva asignatura" si no existe.

Campo 3: "Fecha y Hora de Entrega" (Selector)

Abre un selector de calendario (DatePicker) y luego un selector de hora (TimePicker).

Botón: "Guardar Tarea"

Guarda la información (localmente en la base de datos del dispositivo) y regresa al Dashboard.

Pantalla 3: Asignaturas

Una pantalla de gestión simple para que el usuario configure su semestre.

Componente 1: Listado de Asignaturas

Muestra las asignaturas registradas por el estudiante.

Siguiendo la idea del brief, se presentarán como "Tarjetas de Colores". Cada asignatura tendrá un color distintivo (ej. "Cálculo" en azul, "Física" en rojo) que se usará en el Dashboard para identificar tareas rápidamente.

Funcionalidad:

Añadir nuevas asignaturas (Nombre, Profesor (opcional), Color).

Editar o eliminar asignaturas existentes.

(Opcional) Al tocar una tarjeta, podría filtrar la lista de tareas solo para esa asignatura.

5. Stack Técnico Sugerido (Android)

Lenguaje: Kotlin (Moderno y preferido por Google).

Arquitectura: MVVM (Model-View-ViewModel) o MVI (Model-View-Intent) para una estructura limpia y mantenible.

UI: Jetpack Compose (Declarativo y rápido) o Vistas XML tradicionales.

Base de Datos Local: Room (Parte de Android Jetpack) sobre una base de datos SQLite para persistencia de tareas y asignaturas.

Manejo de Fechas: java.time (LocalDate, LocalDateTime).

Corrutinas de Kotlin para manejar las operaciones de base de datos en hilos secundarios.