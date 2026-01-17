# Guía de Buenas Prácticas para Aplicaciones Móviles Nativas (UniTask)

## 1. Introducción

### Importancia de la optimización

La optimización en aplicaciones móviles es fundamental debido a las limitaciones inherentes de los dispositivos móviles, como la duración de la batería, la capacidad de procesamiento (CPU/GPU) y la memoria RAM limitada. Una aplicación mal optimizada puede provocar un consumo excesivo de batería, sobrecalentamiento y cierres inesperados (Force Close), lo que lleva a la desinstalación por parte del usuario.

### Relación entre rendimiento, calidad y experiencia de usuario

Existe una relación directa y crítica:

- **Calidad de Código:** Un código limpio facilita la detección de errores y el mantenimiento a largo plazo.
- **Rendimiento (Performance):** Tiempos de carga rápidos y animaciones fluidas (60fps) son esenciales.
- **Experiencia de Usuario (UX):** La fluidez y la capacidad de respuesta (responsiveness) generan confianza.

---

## 2. Buenas prácticas de optimización

### Uso eficiente de memoria y recursos

En lugar de cargar todos los elementos en memoria a la vez, se deben utilizar componentes de carga diferida (Lazy Loading).

**Archivo aplicado:** `presentation/ui/screens/DashboardScreen.kt`

```kotlin
// Uso de LazyColumn para reciclar vistas y no saturar la memoria con listas largas
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    // ... secciones de la lista
}
```

### Manejo adecuado del ciclo de vida

Es crucial detener la recolección de flujos (Flows) cuando la aplicación pasa a segundo plano para ahorrar batería y recursos de CPU.

**Archivo aplicado:** `presentation/ui/screens/DashboardScreen.kt`

```kotlin
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = viewModel(factory = AppModule.viewModelFactory),
    // ...
) {
    // collectAsStateWithLifecycle es consciente del ciclo de vida y pausa la recolección
    // cuando la Activity no está al menos en estado STARTED.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ...
}
```

### Organización del código (Arquitectura MVVM)

La arquitectura Model-View-ViewModel (MVVM) separa la lógica de presentación de la interfaz de usuario.

- **ViewModel:** Maneja el estado y la lógica de negocio.
- **View (Composable):** Solo reacciona al estado.

**Archivo aplicado:** `presentation/viewmodel/DashboardViewModel.kt`

```kotlin
class DashboardViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    // ... use cases inyectados
) : ViewModel() {

    // Estado observable para la UI
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // init block inicia la observación de corutinas
    init {
        observeDashboardData()
    }

    // Función que procesa datos en el scope del ViewModel (no bloquea UI)
    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(...) { ... }
                .collect { state -> _uiState.value = state }
        }
    }
}
```

---

## 3. Buenas prácticas en interfaces (Jetpack Compose)

### Uso correcto de estados en Jetpack Compose

Elevar el estado (State Hoisting) permite que los componentes sean reutilizables y fáciles de probar. El componente `DashboardScreen` no posee el estado, lo recibe por parámetros.

**Archivo aplicado:** `presentation/ui/screens/DashboardScreen.kt`

```kotlin
// Componente Stateless (sin estado interno de negocio)
@Composable
fun DashboardScreen(
    state: DashboardUiState,          // Estado recibido
    onAddTaskClick: () -> Unit,       // Evento elevado
    onTaskCompleted: (String) -> Unit, // Evento elevado
    // ...
) {
    // Renderiza la UI basada en 'state'
}
```

### Evitar recomposiciones innecesarias

El uso de claves (`key`) en listas dinámicas ayuda a Compose a identificar qué elementos han cambiado exactamente, evitando redibujar toda la lista.

**Archivo aplicado:** `presentation/ui/screens/DashboardScreen.kt`

```kotlin
// key = { it.id } asegura que Compose rastree los items por ID, no por posición.
items(state.allTasks, key = { it.id }) { task ->
    TaskCard(
        task = task,
        modifier = Modifier.fillMaxWidth().animateContentSize() // Animación eficiente
    )
}
```

### Diseño eficiente de pantallas

Dividir pantallas complejas en pequeños componentes reutilizables (`TaskCard`, `EmptyState`, etc.).

**Archivo aplicado:** `presentation/ui/screens/DashboardScreen.kt`

```kotlin
// Composición modular
item {
    UrgentTasksSection(...)
}
item {
    RewardsBar(...)
}
```

---

## 4. Buenas prácticas en acceso a datos

### Uso adecuado de Room

Room proporciona una capa de abstracción sobre SQLite. El uso de `@ForeignKey` mantiene la integridad referencial (no hay tareas huérfanas si se borra la materia).

**Archivo aplicado:** `data/room/TaskEntity.kt`

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE // Borrado en cascada eficiente a nivel de BD
        )
    ],
    indices = [Index(value = ["subjectId"])] // Índice para agilizar consultas
)
data class TaskEntity(...)
```

### Manejo eficiente de llamadas a APIs y Datos Asíncronos

Aunque actualmente la app es "Offline-First" (prioridad local), se utiliza el patrón `Repository` y `Flow` para manejar datos asíncronos de manera reactiva, estructura lista para integrar API (Retrofit).

**Archivo aplicado:** `presentation/viewmodel/DashboardViewModel.kt`

```kotlin
// Uso de combine para mezclar flujos de datos asíncronos de forma eficiente
combine(
    getUrgentTasksUseCase(),
    getAllTasksUseCase(),
    getSubjectsUseCase(),
    getAllNotificationsUseCase()
) { urgent, all, subjects, notifications ->
    // Transformación de datos en hilo secundario (IO safe)
    // ...
}
```

### Carga responsable de datos

Utilizar estados de carga (`isLoading`) y manejo de errores (`errorMessage`) explícitos para informar al usuario y no bloquear la UI.

**Archivo aplicado:** `presentation/viewmodel/DashboardViewModel.kt`

```kotlin
data class DashboardUiState(
    val urgentTasks: List<TaskUiModel> = emptyList(),
    val allTasks: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = true, // Estado inicial de carga
    val errorMessage: String? = null // Manejo de errores
)
```

---

## 5. Aplicación al proyecto UniTask

En esta actualización del proyecto, se han aplicado prácticas de optimización y arquitectura para asegurar la robustez, el rendimiento y la mantenibilidad de la aplicación. A continuación se documenta en detalle la estructura del proyecto y los cambios implementados, incluyendo el código fuente relevante.

### Estructura de Carpetas y Arquitectura

El proyecto está organizado siguiendo los principios de **Clean Architecture**, dividiendo el código en capas claras de responsabilidad. Esto facilita el testeo y la escalabilidad.

```text
com.example.unitask
├── data                          # Capa de Datos: Fuentes de datos y Repositorios
│   ├── repository                # Implementación de interfaces de repositorio
│   │   ├── RoomTaskRepository.kt
│   │   ├── RoomSubjectRepository.kt
│   │   └── ...
│   └── room                      # Base de datos local (Room)
│       ├── UniTaskDatabase.kt    # Definición de la BD
│       ├── TaskDao.kt            # Consultas SQL para tareas
│       ├── TaskEntity.kt         # Modelo de tabla 'tasks'
│       └── ...
├── di                            # Inyección de Dependencias
│   └── AppModule.kt              # Contenedor manual de dependencias
├── domain                        # Capa de Dominio: Lógica de negocio pura
│   ├── model                     # Modelos de datos del negocio (independientes de framework)
│   │   ├── Task.kt
│   │   └── Subject.kt
│   ├── repository                # Interfaces de repositorios (contratos)
│   │   ├── TaskRepository.kt
│   │   └── ...
│   └── usecase                   # Casos de uso (acciones del usuario)
│       ├── AddTaskUseCase.kt
│       ├── GetAllTasksUseCase.kt
│       └── ...
├── notifications                 # Gestión de Notificaciones y Alarmas
│   ├── AlarmScheduler.kt         # Programador de alarmas
│   ├── NotificationHelper.kt     # Creador de canales de notificación
│   └── ...
├── presentation                  # Capa de Presentación: UI y Estados
│   ├── navigation                # Grafo de navegación de Compose
│   ├── ui
│   │   ├── components            # Componentes reutilizables (Cards, Dialogs)
│   │   ├── screens               # Pantallas completas (Dashboard, AddTask)
│   │   └── theme                 # Definición de temas y colores
│   └── viewmodel                 # ViewModels (State Holders)
│       ├── DashboardViewModel.kt
│       ├── AddTaskViewModel.kt
│       └── ...
├── sensors                       # Gestión de Hardware (Sensores)
│   └── FocusSensorManager.kt     # Lógica de sensores de luz/proximidad
└── ui/theme                      # Configuración visual global (Theme.kt)
```

---

### Detalles de Implementación y Código Fuente

A continuación se presentan implementaciones concretas de buenas prácticas en diferentes áreas del sistema.

#### 1. Persistencia de Datos (Room) - _Integridad Referencial_

**Buenas Prácticas:** Uso de `ForeignKey` para asegurar consistencia (Cascade Delete) e índices para velocidad.

**Archivo:** `data/room/TaskEntity.kt`

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE // SI se borra la materia, se borran sus tareas.
        )
    ],
    // Indexar claves foráneas acelera las consultas de filtrado.
    indices = [Index(value = ["subjectId"])]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subjectId: String,
    val dueDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val isCompleted: Boolean
)
```

#### 2. Gestión de Sensores y Hardware - _Separación de Lógica_

**Buenas Prácticas:** Encapsular la lógica de sensores en una clase dedicada (`Manager`) en lugar de saturar la `Activity` o el `ViewModel`. Uso de `Flow` para emitir cambios de estado del sensor.

**Archivo:** `sensors/FocusSensorManager.kt`

```kotlin
class FocusSensorManager(
    private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    // Sensores del sistema
    private val sensorManager: SensorManager = ...
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Estado expuesto como Flow inmutable
    private val _state = MutableStateFlow(FocusSensorState())
    val state: StateFlow<FocusSensorState> = _state

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values.getOrNull(0) ?: return
            // Lógica de negocio del sensor: determinar si está oscuro
            val dark = lux < lightThreshold
            // Actualización reactiva del estado
            _state.update { it.copy(isDark = dark) }
        }
        // ...
    }

    // Métodos para controlar el ciclo de vida de los sensores (ahorro de batería)
    fun start() { /* Registrar listeners */ }
    fun stop() { /* Desregistrar listeners */ }
}
```

#### 3. Validación de Entrada y Manejo de Errores - _UI Defensiva_

**Buenas Prácticas:** Validar datos en el `ViewModel` antes de invocar la capa de dominio. Uso de "Sealed Interfaces" para tipar errores específicos.

**Archivo:** `presentation/viewmodel/AddTaskViewModel.kt`

```kotlin
fun submit() {
    val current = _uiState.value
    val rawTitle = current.title.trim()

    // 1. Validaciones previas
    if (rawTitle.isBlank()) {
        _uiState.updateDetails { copy(error = AddTaskError.TitleRequired) }
        return
    }

    if (rawTitle.length > MAX_TITLE_LENGTH) {
        _uiState.updateDetails { copy(error = AddTaskError.TitleTooLong) }
        return
    }

    // 2. Ejecución asíncrona segura con runCatching
    viewModelScope.launch {
        _uiState.updateDetails { copy(isSubmitting = true, error = null) }
        runCatching {
            addTaskUseCase(...)
        }
        .onSuccess {
            _events.emit(AddTaskEvent.Success(task.id, isUpdate))
        }
        .onFailure { error ->
            // Manejo centralizado de excepciones
            val submitError = AddTaskError.SubmitError(error.message ?: "Error al guardar")
            _uiState.updateDetails { copy(isSubmitting = false, error = submitError) }
        }
    }
}
```

#### 4. Configuración del Entry Point y Permisos - _Android Moderno_

**Buenas Prácticas:** Manejo de permisos en tiempo de ejecución (Android 13+ Notificaciones) y configuración de dependencias globales.

**Archivo:** `MainActivity.kt`

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configuración Inicial Global
    AppModule.configureAppModule(applicationContext)

    // Gestión de Permisos Android 13 (Tiramisu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val perm = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), 1001)
        }
    }

    // Renderizado Edge-to-Edge para aprovechar toda la pantalla
    enableEdgeToEdge()

    setContent {
        // Inyección de Tema Dinámico
        UniTaskTheme {
            UniTaskApp(...)
        }
    }
}
```

---

### Aspectos a Mejorar (Identificados)

1.  **Inyección de Dependencias Automática (Hilt):**
    Actualmente, `AppModule.kt` es un `object` singleton manual.
    - _Mejora:_ Migrar a **Dagger Hilt** para estandarizar la inyección y facilitar el testing unitario avanzado.
    - _Justificación:_ Elimina el código boilerplate (repetitivo) y gestiona los scopes (Singleton, ViewModelScope) automáticamente.

2.  **Sincronización Remota (Cloud Sync):**
    La app funciona 100% local (Room).
    - _Mejora:_ Implementar `Retrofit` para consumir una API REST y `WorkManager` para sincronizar datos en segundo plano cuando haya conexión.

---

## 6. Conclusión

La aplicación estricta de estas buenas prácticas no es opcional en el desarrollo profesional. Aplicar **Clean Architecture** y optimizaciones de **Ciclo de Vida** antes de publicar asegura que `UniTask` sea robusta, escalable y respetuosa con los recursos del dispositivo del usuario. Un código bien organizado hoy previene deuda técnica mañana y garantiza una base sólida para futuras funcionalidades.
