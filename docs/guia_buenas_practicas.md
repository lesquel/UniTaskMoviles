# Guía de Buenas Prácticas para Aplicaciones Móviles Nativas (UniTask)

## 1. Introducción

### Importancia de la optimización

La optimización en aplicaciones móviles es fundamental debido a las limitaciones de los dispositivos (batería, procesamiento, memoria) y la inestabilidad de las redes. Una aplicación optimizada no solo consume menos recursos, sino que responde más rápido y es más fiable.

### Relación entre rendimiento, calidad y experiencia de usuario (UX)

Existe una correlación directa:

- **Calidad de Código:** Un código limpio y estructurado (Clean Architecture) facilita el mantenimiento y reduce errores (bugs).
- **Rendimiento:** Un uso eficiente del ciclo de vida y la memoria evita "crashes" y ralentizaciones (ANR).
- **Experiencia:** El usuario percibe la fluidez. Si la app preserva sus datos y no consume batería excesiva en segundo plano, la retención del usuario mejora.

---

## 2. Buenas prácticas de optimización

### Uso eficiente de memoria y recursos

- **Evitar fugas de memoria:** Usar `WeakReference` si es necesario, aunque en Kotlin/Compose el uso de corutinas estructuradas (`viewModelScope`) mitiga esto.
- **Carga diferida (Lazy Loading):** En listas largas, usar `LazyColumn` en lugar de `Column` con scroll para reciclar vistas.
- **Imágenes:** Usar librerías como Coil o Glide que gestionan caché y redimensionamiento automático.

### Manejo adecuado del ciclo de vida

- Respetar los estados del `Activity`/`Fragment` (CREATED, STARTED, RESUMED).
- Detener operaciones costosas (GPS, animaciones, recolección de flujos) cuando la app pasa a segundo plano (`onStop`).

### Organización del código (Arquitectura MVVM)

- **Separación de responsabilidades:**
  - **Model:** Datos y lógica de negocio.
  - **View:** UI (Compose functions).
  - **ViewModel:** Gestor de estado intermediario.
- Esto permite probar la lógica sin depender del framework de Android y facilita cambios futuros en la UI sin romper las reglas de negocio.

---

## 3. Buenas prácticas en interfaces (Jetpack Compose)

### Uso correcto de estados

- **State Hoisting (Elevación de estado):** Mover el estado al ancestro común más cercano (generalmente el `ViewModel` o una función "Route"). Esto hace que los componentes de UI sean **stateless** (sin estado) y reutilizables.
- **Inmutabilidad:** Los estados deben ser inmutables (`data class` con `val`) y modificarse creando copias (`copy()`), permitiendo a Compose detectar cambios eficientemente.

### Evitar recomposiciones innecesarias

- Usar `remember` y `derivedStateOf` para cálculos costosos dentro de composables.
- Usar claves estables (`key`) en listas `LazyColumn`.
- Usar `@Stable` y `@Immutable` para ayudar al compilador de Compose a omitir recomposiciones de componentes cuyos datos no han cambiado.

### Diseño eficiente de pantallas

- Evitar anidamientos profundos de Layouts.
- Modularizar pantallas grandes en componentes pequeños y testeables (`TaskCard`, `SubjectSelector`).

---

## 4. Buenas prácticas en acceso a datos

### Uso adecuado de Room

- **Persistencia Local:** Usar Room para guardar datos críticos. Esto permite que la app sea "Offline-First" (funcione sin internet).
- **Consultas Asíncronas:** Nunca acceder a la BD en el hilo principal (Main Thread). Usar `suspend functions` y `Flow`.
- **Relaciones:** Usar `@Relation` o `@ForeignKey` para mantener integridad referencial (ej: Tareas borradas si se borra la Asignatura).

### Manejo eficiente de llamadas a APIs

- Usar **Retrofit** con **Coroutines**.
- Patrón **Repository**: El repositorio decide si buscar datos en local (Room) o remoto (API), abstrayendo esa lógica de la UI.

### Carga responsable de datos

- Implementar **paginación** (Paging 3) para grandes volúmenes de datos.
- Gestionar estados de Carga (`Loading`), Éxito (`Success`) y Error (`Error`) explícitamente en la UI.

---

## 5. Aplicación al proyecto UniTask

En esta actualización del proyecto, se han aplicado prácticas de optimización y arquitectura para asegurar la robustez, el rendimiento y la mantenibilidad de la aplicación. A continuación se documenta en detalle la estructura del proyecto y los cambios implementados, incluyendo el código fuente relevante.

### Estructura del Proyecto

El proyecto sigue una arquitectura limpia (Clean Architecture) con separación de capas: `domain`, `data`, y `presentation`.

```text
c:\Users\lesqu\AndroidStudioProjects\UniTask\app\src\main\java\com\example\unitask
│   MainActivity.kt
│
├───data  (Capa de Datos: Repositorios y Fuentes de Datos)
│   ├───repository
│   │       RoomTaskRepository.kt       <-- NUEVO: Implementación con Room
│   │       RoomSubjectRepository.kt    <-- NUEVO: Implementación con Room
│   │       SharedPrefsNotificationRepository.kt
│   │       ...
│   └───room                            <-- NUEVO: Paquete para Room Database
│           UniTaskDatabase.kt          <-- Base de Datos Principal
│           TaskDao.kt                  <-- Data Access Object para Tareas
│           TaskEntity.kt               <-- Tabla Tareas (SQL)
│           SubjectDao.kt               <-- Data Access Object para Asignaturas
│           SubjectEntity.kt            <-- Tabla Asignaturas (SQL)
│           Converters.kt               <-- Convertidores de Tipos (Fechas)
│
├───di    (Inyección de Dependencias)
│       AppModule.kt                    <-- ACTUALIZADO: Provee la BD y Repositorios
│
├───domain (Capa de Dominio: Reglas de Negocio)
│   ├───model
│   │       Task.kt
│   │       Subject.kt
│   └───usecase                         <-- Casos de Uso (Lógica Pura)
│           AddTaskUseCase.kt
│           ...
│
└───presentation (Capa de Presentación: UI y ViewModels)
    ├───ui
    │   ├───screens
    │   │       DashboardScreen.kt      <-- OPTIMIZADO: Ciclo de Vida
    │   │       AddTaskScreen.kt        <-- MEJORADO: Manejo de Errores
    │   │       ...
    └───viewmodel
            AddTaskViewModel.kt         <-- REFACTORIZADO: Validaciones
            ...
```

---

### Detalle de Implementación y Archivos Modificados

A continuación se detalla cada componente implementado para cumplir con los objetivos de optimización.

#### 1. Persistencia de Datos (Room) - _Optimización de Datos_

**Objetivo:** Reemplazar el almacenamiento en memoria volátil por una base de datos SQLite persistente, garantizando que los datos sobrevivan al cierre de la aplicación.

**Archivos Implementados:**

**`data/room/UniTaskDatabase.kt`**
Define la conexión a la base de datos y expone los DAOs.

```kotlin
package com.example.unitask.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main database holder for the application.
 * Manages the connection to the SQLite database and provides DAOs.
 */
@Database(
    entities = [TaskEntity::class, SubjectEntity::class],
    version = 1,
    exportSchema = false
)
// Registers custom type converters (e.g. for LocalDateTime <-> String).
@TypeConverters(Converters::class)
abstract class UniTaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val subjectDao: SubjectDao
}
```

**`data/room/TaskEntity.kt`**
Modela la tabla `tasks` en SQL. Se utiliza una **Foreign Key** con `CASCADE` para mantener la integridad referencial: si se borra una asignatura, se borran sus tareas.

```kotlin
package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Indexing foreign keys is a best practice for query performance.
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

// Mapping extensions
fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        subjectId = subjectId,
        dueDateTime = dueDateTime,
        createdAt = createdAt,
        isCompleted = isCompleted
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        subjectId = subjectId,
        dueDateTime = dueDateTime,
        createdAt = createdAt,
        isCompleted = isCompleted
    )
}
```

**`data/repository/RoomTaskRepository.kt`**
Implementa el patrón repositorio conectando la base de datos (Data Layer) con el dominio. Utiliza `Flow` para actualizaciones reactivas.

```kotlin
package com.example.unitask.data.repository

import com.example.unitask.data.room.TaskDao
import com.example.unitask.data.room.toDomain
import com.example.unitask.data.room.toEntity
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [TaskRepository] implementation using Room as the persistent data source.
 */
class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {

    override fun getTasksFlow(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    // ... completeTask, deleteTask implementation ...
}
```

#### 2. Inyección de Dependencias - _Configuración Global_

**Objetivo:** Proveer la instancia única de la base de datos a toda la aplicación.

**Archivo Modificado: `di/AppModule.kt`**
Se modificó para inicializar la base de datos Room en lugar de los repositorios en memoria.

```kotlin
object AppModule {

    private var _database: UniTaskDatabase? = null

    // Helper to access DB safely
    private val database: UniTaskDatabase
        get() = _database ?: throw IllegalStateException("AppModule not configured")

    // Data sources: Ahora usan Room
    private val subjectRepository: SubjectRepository by lazy {
        RoomSubjectRepository(database.subjectDao)
    }

    private val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(database.taskDao)
    }

    fun configureAppModule(context: Context) {
        _appContext = context.applicationContext

        // Initialize Room Database
        if (_database == null) {
            _database = Room.databaseBuilder(
                context.applicationContext,
                UniTaskDatabase::class.java,
                "unitask_database"
            ).build()
        }
        // ...
    }
}
```

#### 3. Optimización de UI y Ciclo de Vida - _Eficiencia de Recursos_

**Objetivo:** Evitar que la UI procese datos cuando la aplicación está en segundo plano (suspendida), ahorrando batería.

**Archivo Modificado: `presentation/ui/screens/DashboardScreen.kt`**
Se reemplazó `collectAsState` por `collectAsStateWithLifecycle`.

```kotlin
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = viewModel(factory = AppModule.viewModelFactory),
    // ...
) {
    // OPTIMIZACIÓN: collectAsStateWithLifecycle detiene la recolección flow
    // cuando la app va a segundo plano (Lifecycle STOPPED).
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ...
}
```

#### 4. Calidad de Código - _Manejo de Errores Tipados_

**Objetivo:** Eliminar "hardcoded strings" y mejorar la robustez del manejo de errores.

**Archivo Modificado: `presentation/viewmodel/AddTaskViewModel.kt`**
Se introdujo una interfaz sellada (`sealed interface`) para los errores.

```kotlin
sealed interface AddTaskError {
    data object TitleRequired : AddTaskError
    data object TitleTooLong : AddTaskError
    data object SubjectRequired : AddTaskError
    data object DateTimeRequired : AddTaskError
    data class SubmitError(val message: String) : AddTaskError
}

// En el ViewModel:
fun onTitleChanged(value: String) {
    if (value.length > MAX_TITLE_LENGTH) {
        // Uso de tipo seguro en lugar de String directo
        _uiState.updateDetails { copy(error = AddTaskError.TitleTooLong) }
        return
    }
    // ...
}
```

---

## 6. Conclusión

La aplicación de estas buenas prácticas transforma UniTask de un prototipo funcional a una aplicación robusta y profesional. La introducción de persistencia (Room) garantiza que el usuario no pierda trabajo, mientras que las optimizaciones de UI (Lifecycle) y código (Strings) aseguran que la app sea mantenible y eficiente energéticamente. Publicar una app sin estos estándares resultaría en malas reseñas por pérdida de datos o consumo excesivo de batería.
