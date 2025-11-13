# Plan de implementación — UniTask (Kotlin + Jetpack Compose + Clean Architecture)

Este documento describe en detalle el plan para implementar la aplicación UniTask usando Kotlin, Jetpack Compose y Clean Architecture. La persistencia será en memoria (variables/StateFlow) para un prototipo rápido y fácil de probar. El diseño estará preparado para sustituir la capa de datos por Room en el futuro.

## Resumen rápido

- Arquitectura: Clean Architecture (Domain / Data / Presentation).
- UI: Jetpack Compose.
- Persistencia (prototipo): en memoria usando MutableStateFlow / SnapshotStateList.
- Objetivo: app minimalista para registrar tareas por "Asignatura" y priorizar tareas urgentes (próximas 24-48h).

---

## Contrato breve (inputs/outputs, criterios de éxito)

- Entradas principales:
  - Crear tarea: (title: String, subjectId: String, dueDateTime: LocalDateTime)
  - Crear asignatura: (name: String, colorHex: String, teacher: String?)
- Salidas:
  - Flujos observables de lista de tareas y lista de asignaturas (StateFlow / Flow / Compose State).
- Criterios de éxito:
  - UI muestra la sección "Urgente" con tareas próximas (24-48h) y una lista ordenada cronológicamente.
  - Añadir/Completar/Eliminar tareas actualiza la UI instantáneamente.
  - Todo en memoria (sin I/O); estado persistente durante la vida del proceso (ViewModel).

---

## Edge cases importantes

- Título vacío → bloquear guardado y mostrar validación.
- Fecha/hora pasada → advertir o prevenir.
- Múltiples tareas en la misma fecha/hora → ordenar por fecha, luego por título.
- Eliminación de asignatura con tareas asociadas → mostrar confirmación y decidir si eliminar tareas o impedirlo (recomiendo confirmar y eliminar tareas asociadas o reasignar).
- Zonas horarias / cambios de hora → usar LocalDateTime y comparar en la zona del dispositivo.
- Escala: en memoria, datos se perderán al cerrar la app (aceptable para prototipo). Preparar interfaz para persistencia futura.

---

## Estructura de carpetas recomendada (paquete base: `com.example.unitask`)

- `app/src/main/java/com/example/unitask/`
  - `domain/`
    - `model/`
      - `Task.kt`
      - `Subject.kt`
    - `repository/`
      - `TaskRepository.kt` (interface)
      - `SubjectRepository.kt` (interface)
    - `usecase/`
      - `GetUrgentTasksUseCase.kt`
      - `GetAllTasksUseCase.kt`
      - `AddTaskUseCase.kt`
      - `CompleteTaskUseCase.kt`
      - `GetSubjectsUseCase.kt`
      - `AddSubjectUseCase.kt`
      - `EditSubjectUseCase.kt`
      - `DeleteSubjectUseCase.kt`
  - `data/`
    - `model/` (DTOs si fuesen necesarios)
    - `repository/`
      - `InMemoryTaskRepository.kt`
      - `InMemorySubjectRepository.kt`
  - `presentation/`
    - `ui/`
      - `screens/`
        - `DashboardScreen.kt`
        - `AddTaskScreen.kt`
        - `SubjectsScreen.kt`
      - `components/`
        - `TaskCard.kt`
        - `SubjectChip.kt`
        - `UrgentCarousel.kt`
    - `viewmodel/`
      - `DashboardViewModel.kt`
      - `AddTaskViewModel.kt`
      - `SubjectsViewModel.kt`
    - `navigation/`
      - `NavGraph.kt`
  - `di/`
    - `AppModule.kt` (o wiring manual)
  - `util/`
    - `DateTimeUtils.kt`
  - `MainActivity.kt`
  - `theme/`
    - `Theme.kt`, `Color.kt`, `Type.kt`

---

## Modelos (domain/model)

- Task (data class):

  - `id: String` (UUID)
  - `title: String`
  - `subjectId: String`
  - `dueDateTime: LocalDateTime`
  - `createdAt: LocalDateTime`
  - `isCompleted: Boolean`

- Subject (data class):
  - `id: String` (UUID)
  - `name: String`
  - `colorHex: String`
  - `teacher: String?`

Notas:

- Mantén las clases del dominio inmutables (`data class` con `val`).
- La lógica de negocio se encapsula en UseCases y repositorios.

---

## Repositorios (interfaces en `domain/repository`)

- `TaskRepository`:

  - `fun getTasksFlow(): Flow<List<Task>>`
  - `suspend fun addTask(task: Task)`
  - `suspend fun completeTask(taskId: String)`
  - `suspend fun deleteTask(taskId: String)`
  - `suspend fun updateTask(task: Task)`

- `SubjectRepository`:
  - `fun getSubjectsFlow(): Flow<List<Subject>>`
  - `suspend fun addSubject(subject: Subject)`
  - `suspend fun editSubject(subject: Subject)`
  - `suspend fun deleteSubject(subjectId: String)`

¿Por qué `Flow`? Porque ViewModel y Compose consumen flujos reactivos. Usar `StateFlow` internamente para emitir el estado actual hace más simple la integración con Compose.

---

## Implementación en memoria (data/repository)

- `InMemoryTaskRepository`:

  - Internamente:
    - `private val _tasks = MutableStateFlow<List<Task>>(initialSample)`
    - `override fun getTasksFlow() = _tasks.asStateFlow()`
  - Operaciones:
    - `addTask`: `_tasks.update { it + task }`
    - `completeTask`: mapear y setear `isCompleted = true`
    - `deleteTask`: filtrar
    - `updateTask`: reemplazar por id

- Concurrencia:

  - Usar `withContext(Dispatchers.Default)` dentro de métodos `suspend` y `kotlinx.coroutines.flow.update` para operaciones atómicas.

- Extras:
  - Helpers para `reset()` y `seedData()` para desarrollo y tests.

Notas sobre Compose:

- En ViewModel es conveniente convertir `Flow<List<Task>>` a `StateFlow` o usar `collectAsState()` en Composables para integrarlo con el sistema de snapshots de Compose.

---

## UseCases (domain/usecase)

- Cada use case realiza una responsabilidad única. Ejemplos:

  - `GetUrgentTasksUseCase`:
    - Input: período (Duration) opcional.
    - Output: `Flow<List<Task>>` filtrando tareas no completadas con `dueDateTime <= now + periodo`.
  - `AddTaskUseCase`:
    - Validaciones: título no vacío, fecha futura.
  - `CompleteTaskUseCase`:
    - Marca como completado y actualiza repositorio.

- Implementar UseCases como clases inmutables que reciben repositorios por constructor (inyección de dependencias).

---

## ViewModels (presentation/viewmodel)

- Principios:

  - Exponer `UiState` inmutable (data class) con campos necesarios.
  - Usar `StateFlow`/`MutableStateFlow` y `viewModelScope`.
  - Transformar Domain -> UI models (resolve `subjectName` y `subjectColor` en ViewModel).

- Ejemplo `DashboardViewModel`:
  - Inyectar `GetAllTasksUseCase`, `GetUrgentTasksUseCase`, `GetSubjectsUseCase`.
  - Combinar flujos con `combine()` para producir `DashboardUiState`.
  - Exponer eventos (SharedFlow) para snackbars o navegación.

---

## UI (Jetpack Compose)

- Navigation: `navigation-compose` con destinos: `dashboard`, `addTask`, `subjects`.

- Screens:

  - `DashboardScreen`:
    - `UrgentCarousel` (tareas próximas 24-48h, horizontal).
    - `LazyColumn` con todas las tareas pendientes ordenadas.
    - Checkbox en `TaskCard` para marcar completada (desaparece tras completar).
    - `FAB` para navegar a `AddTaskScreen`.
  - `AddTaskScreen`:
    - `TextField` para título.
    - Selector de `Subject` (DropdownMenu o Dialog con opción `+ Añadir asignatura`).
    - Selector de fecha y hora (DatePicker + TimePicker).
    - Validaciones inline y botón `Guardar`.
  - `SubjectsScreen`:
    - Listado de tarjetas por asignatura con color.
    - Editar / Eliminar con `AlertDialog` de confirmación.

- Componentes:
  - `TaskCard` (título, chip de asignatura, hora de entrega, checkbox).
  - `SubjectChip` (círculo con color y nombre).
  - `UrgentCarousel` (LazyRow con tarjetas).

---

## Manejo de fechas y lógica "Urgente"

- Usar `java.time.LocalDateTime` y `java.time.Duration`.
- Utilidad `DateTimeUtils` con:
  - `fun isUrgent(due: LocalDateTime, now: LocalDateTime = LocalDateTime.now(), withinHours: Long = 24): Boolean`
  - `fun formatForUi(due: LocalDateTime): String`
- Definición recomendada: urgente = dentro de 48 horas; destacar próximas 24h.

---

## Inyección de dependencias (di)

- Prototipo: wiring manual en `AppModule` (objetos singleton en proceso).
- Producción: reemplazar por Hilt (módulos `@Provides` / `@Binds`).

Ejemplo `AppModule` manual:

- Crear instancias de `InMemorySubjectRepository`, `InMemoryTaskRepository`, UseCases y ViewModels.

---

## Tests

- Unit tests (JVM) para UseCases y repositorios con `kotlinx-coroutines-test`.
- Compose tests para pantallas: renderizar `DashboardScreen` con datos de ejemplo y verificar que "Urgente" aparece.
- Herramientas: JUnit4/5, AndroidX Test, Compose Testing, `kotlinx-coroutines-test`.

Pruebas sugeridas:

- `GetUrgentTasksUseCase_happyPath`
- `AddTask_invalidTitle_validationError`
- `InMemoryTaskRepository_addAndGet_tasksReturned`
- Compose: `Dashboard_showsUrgentAndAllTasks`

---

## Calidad / Quality Gates

- Build: Gradle sync y build PASS.
- Lint: Android Lint PASS.
- Tests: Unit + Compose tests PASS.
- Añadir `detekt` opcional para análisis estático.

---

## Extras proactivos (recomendados)

- Seed data con 3 asignaturas y 8 tareas de ejemplo para desarrollo.
- `Undo` (Snackbar) al completar tarea usando `SharedFlow` de eventos.
- Export/Import JSON para persistencia simple si no se quiere integrar Room aún.

---

## Flujo de implementación (día a día sugerido)

1. Día 1: Modelos del dominio + interfaces de repositorio + InMemory repos. Seed data.
2. Día 2: UseCases + unit tests iniciales (GetAll, Add).
3. Día 3: ViewModels + wiring (AppModule) + sample DI.
4. Día 4: UI Compose Dashboard + TaskCard + consumo del ViewModel.
5. Día 5: AddTaskScreen + pickers + validaciones + navegación.
6. Día 6: SubjectsScreen + editar/eliminar + integración.
7. Día 7: Tests Compose, polish UI, accesibilidad y theming.
8. Día 8: Documentación y preparar PR.

---

## Artefactos a entregar

- Código completo con la estructura propuesta.
- Tests unitarios y de UI básicos.
- `README.md` con:
  - Cómo ejecutar (Android Studio, emulador).
  - Cómo seedear datos.
  - Cómo cambiar la capa de datos a Room (instrucciones paso a paso).
- `decisions.md` con notas de diseño y trade-offs.

---

## Cómo exponer el estado a Compose (ejemplo conceptual)

- Repositorio: `_tasks: MutableStateFlow<List<Task>>`
- UseCase: `fun invoke(): Flow<List<Task>> = taskRepo.getTasksFlow()`
- ViewModel: `val uiState = combine(tasksFlow, subjectsFlow) { tasks, subjects -> mapToUiState(...) }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)`
- Compose: `val state by viewModel.uiState.collectAsState()`

---

## Consideraciones finales y recomendaciones

- En memoria es ideal para prototipo y pruebas. El estado se pierde al terminar el proceso.
- Mantener separación clara entre UI y dominio: lógica de negocio en UseCases.
- Preparar repositorios para ser reemplazados por Room (mantener interfaces estables).

---

## Próximos pasos (puedes elegir uno)

- Opción A: Genero plantillas de los archivos Kotlin principales (`domain` models, repositorio interfaces e implementación en memoria) y los aplico en el proyecto.
- Opción B: Empiezo implementando sólo los modelos y repositorios en memoria (paso 1).
- Opción C: Te doy un checklist para comenzar a codificar en Android Studio.

Dime cuál prefieres y lo hago ahora.
