# 📚 UniTask - Documentación Completa

## Índice
1. [Descripción de la Aplicación](#descripción-de-la-aplicación)
2. [Características Principales](#características-principales)
3. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
4. [Estructura de Carpetas](#estructura-de-carpetas)
5. [Tecnologías Utilizadas](#tecnologías-utilizadas)
6. [Flujos de la Aplicación](#flujos-de-la-aplicación)
7. [Base de Datos](#base-de-datos)
8. [Sistema de Notificaciones](#sistema-de-notificaciones)
9. [Sistema de Recompensas](#sistema-de-recompensas)
10. [Iconos y Recursos](#iconos-y-recursos)
11. [Proceso de Construcción](#proceso-de-construcción)
12. [Publicación en Play Store](#publicación-en-play-store)

---

## Descripción de la Aplicación

### ¿Qué es UniTask?

**UniTask** es una aplicación móvil nativa para Android diseñada especialmente para estudiantes universitarios que necesitan organizar sus tareas académicas de manera eficiente. La aplicación permite gestionar asignaturas, crear tareas con fechas de entrega, recibir recordatorios mediante alarmas y competir con otros usuarios a través de un sistema de gamificación.

### Descripción Larga (Para Play Store)

```
📚 UniTask - Tu Compañero de Estudio Definitivo

¿Cansado de olvidar entregas importantes? ¿Quieres mejorar tu organización académica? 
UniTask es la solución perfecta para estudiantes que buscan gestionar sus tareas 
universitarias de manera inteligente y divertida.

✨ CARACTERÍSTICAS PRINCIPALES:

📋 GESTIÓN DE TAREAS
• Crea tareas con título, fecha y hora de entrega
• Asigna cada tarea a una materia específica
• Visualiza tareas urgentes en las próximas 48 horas
• Filtra tareas por día de la semana
• Marca tareas como completadas y gana recompensas

📚 ORGANIZACIÓN POR MATERIAS
• Crea asignaturas con colores personalizados
• Asigna profesores a cada materia
• Visualiza todas las tareas de cada asignatura
• Colores identificativos para mejor organización

⏰ SISTEMA DE ALARMAS INTELIGENTE
• Configura múltiples recordatorios por tarea
• Elige entre plantillas predefinidas (5 min, 30 min, 1 hora, 24 horas)
• Recibe notificaciones incluso con la app cerrada
• Las alarmas se restauran automáticamente al reiniciar el dispositivo

🏆 SISTEMA DE GAMIFICACIÓN
• Gana XP por cada tarea completada
• Sube de nivel mientras completas tus tareas
• Compite en el ranking con otros usuarios
• Mantén tu racha diaria de productividad

👤 PERFIL PERSONALIZADO
• Crea tu cuenta con foto de perfil
• Visualiza tus estadísticas de productividad
• Consulta tu nivel, XP total y racha actual
• Sincroniza tu progreso en la nube

🎨 DISEÑO MODERNO
• Interfaz Material Design 3
• Soporte para tema claro y oscuro
• Animaciones fluidas y atractivas
• Pull-to-refresh para actualizar datos

UniTask transforma la gestión de tareas en una experiencia motivadora. 
¡Descárgala ahora y nunca más olvides una entrega!

Desarrollado con ❤️ para estudiantes, por estudiantes.
```

### Público Objetivo
- Estudiantes universitarios
- Estudiantes de preparatoria/bachillerato
- Cualquier persona que necesite organizar tareas con fechas de entrega

---

## Características Principales

### 1. Sistema de Usuarios
- **Registro**: Creación de cuenta con usuario, email y contraseña
- **Login**: Acceso con usuario/email y contraseña
- **Perfil**: Edición de datos personales y foto de perfil
- **Sesión persistente**: El usuario permanece logueado

### 2. Gestión de Asignaturas
- Crear asignaturas con nombre, color y profesor
- Selector de color visual con 16 colores predefinidos
- Editar y eliminar asignaturas
- Eliminación en cascada de tareas asociadas

### 3. Gestión de Tareas
- Crear tareas con título, materia y fecha/hora de entrega
- Las tareas pertenecen al usuario que las crea
- Bloqueo de fechas pasadas en el selector
- Validación de campos en tiempo real
- Límite de 50 caracteres para el título

### 4. Sistema de Alarmas
- Plantillas predefinidas (5 min, 30 min, 1 hora, 24 horas)
- Notificaciones con información de la tarea
- Vibración al recibir recordatorio
- Funcionamiento en segundo plano
- Restauración de alarmas al reiniciar dispositivo

### 5. Sistema de Recompensas
- +25 XP por tarea completada
- Niveles basados en XP acumulado
- Racha de días consecutivos
- Ranking/Leaderboard de usuarios

### 6. Interfaz de Usuario
- Material Design 3
- Tema claro y oscuro
- Pull-to-refresh en todas las pantallas
- Navegación inferior con 5 secciones
- Animaciones y transiciones suaves

---

## Arquitectura del Proyecto

### Patrón MVVM + Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Screens   │  │ ViewModels  │  │    Components       │  │
│  │  (Compose)  │◄─┤  (State)    │  │   (Reusables)       │  │
│  └─────────────┘  └──────┬──────┘  └─────────────────────┘  │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                    DOMAIN LAYER                              │
│  ┌─────────────┐  ┌──────┴──────┐  ┌─────────────────────┐  │
│  │   Models    │  │  Use Cases  │  │   Repositories      │  │
│  │  (Entities) │  │  (Business) │  │   (Interfaces)      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                     DATA LAYER                               │
│  ┌─────────────┐  ┌──────┴──────┐  ┌─────────────────────┐  │
│  │    Room     │  │ Repository  │  │   SharedPrefs       │  │
│  │  Database   │  │   Impl      │  │   (Notifications)   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de Datos

```kotlin
// 1. El usuario interactúa con la UI (Screen)
// 2. El Screen llama a una función del ViewModel
// 3. El ViewModel ejecuta un UseCase
// 4. El UseCase accede al Repository
// 5. El Repository obtiene/guarda datos en Room/SharedPrefs
// 6. Los datos fluyen de vuelta al ViewModel (StateFlow)
// 7. La UI se actualiza automáticamente (Compose recompone)
```

---

## Estructura de Carpetas

```
app/src/main/java/com/example/unitask/
│
├── 📁 data/                          # Capa de datos
│   ├── 📁 repository/                # Implementaciones de repositorios
│   │   ├── RoomTaskRepository.kt
│   │   ├── RoomSubjectRepository.kt
│   │   ├── RoomUserRepository.kt
│   │   ├── SharedPrefsNotificationRepository.kt
│   │   └── SharedPrefsRewardRepository.kt
│   │
│   └── 📁 room/                      # Base de datos Room
│       ├── UniTaskDatabase.kt        # Configuración de BD
│       ├── TaskEntity.kt             # Entidad de tareas
│       ├── SubjectEntity.kt          # Entidad de materias
│       ├── UserEntity.kt             # Entidad de usuarios
│       ├── AlarmTemplateEntity.kt    # Plantillas de alarma
│       ├── TaskDao.kt                # DAO de tareas
│       ├── SubjectDao.kt             # DAO de materias
│       └── UserDao.kt                # DAO de usuarios
│
├── 📁 domain/                        # Capa de dominio
│   ├── 📁 model/                     # Modelos de negocio
│   │   ├── Task.kt
│   │   ├── Subject.kt
│   │   ├── User.kt
│   │   ├── NotificationSetting.kt
│   │   └── AlarmTemplate.kt
│   │
│   ├── 📁 repository/                # Interfaces de repositorios
│   │   ├── TaskRepository.kt
│   │   ├── SubjectRepository.kt
│   │   ├── UserRepository.kt
│   │   ├── NotificationRepository.kt
│   │   └── RewardRepository.kt
│   │
│   └── 📁 usecase/                   # Casos de uso
│       ├── AddTaskUseCase.kt
│       ├── GetAllTasksUseCase.kt
│       ├── CompleteTaskUseCase.kt
│       ├── ScheduleAlarmUseCase.kt
│       └── ... (otros casos de uso)
│
├── 📁 presentation/                  # Capa de presentación
│   ├── 📁 navigation/                # Configuración de navegación
│   │   └── NavGraph.kt
│   │
│   ├── 📁 ui/
│   │   ├── 📁 screens/               # Pantallas de la app
│   │   │   ├── DashboardScreen.kt    # Pantalla principal
│   │   │   ├── AddTaskScreen.kt      # Crear/editar tarea
│   │   │   ├── SubjectsScreen.kt     # Gestión de materias
│   │   │   ├── ProfileScreen.kt      # Perfil de usuario
│   │   │   ├── LeaderboardScreen.kt  # Ranking
│   │   │   ├── LoginScreen.kt        # Inicio de sesión
│   │   │   ├── RegisterScreen.kt     # Registro
│   │   │   └── AlarmSettingsScreen.kt# Config. alarmas
│   │   │
│   │   ├── 📁 components/            # Componentes reutilizables
│   │   │   ├── TaskCard.kt           # Tarjeta de tarea
│   │   │   ├── RewardsBar.kt         # Barra de XP/nivel
│   │   │   ├── BottomNavBar.kt       # Navegación inferior
│   │   │   ├── ColorPicker.kt        # Selector de color
│   │   │   ├── AppHeader.kt          # Header reutilizable
│   │   │   ├── DayFilterChips.kt     # Filtros por día
│   │   │   └── EmptyState.kt         # Estado vacío
│   │   │
│   │   └── 📁 theme/                 # Tema de la app
│   │       ├── Theme.kt
│   │       ├── Color.kt
│   │       └── Type.kt
│   │
│   └── 📁 viewmodel/                 # ViewModels
│       ├── DashboardViewModel.kt
│       ├── AddTaskViewModel.kt
│       ├── SubjectsViewModel.kt
│       ├── ProfileViewModel.kt
│       ├── AuthViewModel.kt
│       ├── LeaderboardViewModel.kt
│       └── RewardsViewModel.kt
│
├── 📁 notifications/                 # Sistema de notificaciones
│   ├── AlarmReceiver.kt              # Receptor de alarmas
│   ├── AlarmScheduler.kt             # Programador de alarmas
│   ├── NotificationHelper.kt         # Helper de notificaciones
│   ├── RescheduleWorker.kt           # Worker para reinicio
│   └── AlarmManagerWrapper.kt        # Wrapper para tests
│
├── 📁 di/                            # Inyección de dependencias
│   └── AppModule.kt                  # Configuración manual DI
│
└── MainActivity.kt                   # Activity principal
```

---

## Tecnologías Utilizadas

### Lenguaje y Framework
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Kotlin | 2.0.21 | Lenguaje de programación principal |
| Jetpack Compose | BOM 2024.12.01 | UI declarativa moderna |
| Material 3 | Incluido en Compose | Diseño Material Design 3 |

### Android Jetpack
| Librería | Uso |
|----------|-----|
| Navigation Compose | Navegación entre pantallas |
| Room | Base de datos local SQLite |
| ViewModel | Gestión del estado de UI |
| Lifecycle | Ciclo de vida de componentes |
| DataStore | Preferencias del usuario |
| WorkManager | Tareas en segundo plano |

### Configuración del Proyecto

```kotlin
// build.gradle.kts (app)
android {
    namespace = "com.example.unitask"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.unitask"
        minSdk = 24           // Android 7.0 (Nougat)
        targetSdk = 36        // Android 15
        versionCode = 1
        versionName = "1.0"
    }
}
```

---

## Flujos de la Aplicación

### 1. Flujo de Autenticación

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Splash    │────►│   Login     │────►│  Dashboard  │
│   Screen    │     │   Screen    │     │   (Home)    │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                    ┌──────▼──────┐
                    │  Register   │
                    │   Screen    │
                    └─────────────┘
```

**Código del flujo de login:**

```kotlin
// AuthViewModel.kt
fun login() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        // Validaciones
        if (state.usernameOrEmail.isBlank()) {
            _events.send(AuthEvent.Error("Ingresa tu usuario o correo"))
            return@launch
        }
        
        // Llamada al repositorio
        val result = userRepository.login(
            state.usernameOrEmail.trim(), 
            state.password
        )
        
        result.fold(
            onSuccess = { _events.send(AuthEvent.LoginSuccess) },
            onFailure = { _events.send(AuthEvent.Error(it.message)) }
        )
    }
}
```

### 2. Flujo de Creación de Tarea

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Dashboard  │────►│  AddTask    │────►│   Guardar   │
│   (FAB +)   │     │   Screen    │     │   Tarea     │
└─────────────┘     └──────┬──────┘     └──────┬──────┘
                           │                    │
                    ┌──────▼──────┐      ┌──────▼──────┐
                    │  Validar    │      │  Programar  │
                    │  Campos     │      │  Alarmas    │
                    └─────────────┘      └─────────────┘
```

**Código de validación:**

```kotlin
// AddTaskViewModel.kt - submit()
fun submit() {
    val current = _uiState.value
    val rawTitle = current.title.trim()

    // Validaciones
    if (rawTitle.isBlank()) {
        _uiState.updateDetails { copy(error = AddTaskError.TitleRequired) }
        return
    }

    if (rawTitle.length > MAX_TITLE_LENGTH) {
        _uiState.updateDetails { copy(error = AddTaskError.TitleTooLong) }
        return
    }

    if (subjectId == null) {
        _uiState.updateDetails { copy(error = AddTaskError.SubjectRequired) }
        return
    }

    // Guardar tarea y programar alarmas...
}
```

### 3. Flujo de Notificaciones

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Crear     │────►│  Programar  │────►│   Alarma    │
│   Tarea     │     │  Alarma     │     │  Dispara    │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
┌─────────────┐     ┌─────────────┐     ┌──────▼──────┐
│   Abrir     │◄────│Notificación │◄────│   Alarm     │
│    App      │     │  Mostrada   │     │  Receiver   │
└─────────────┘     └─────────────┘     └─────────────┘
```

---

## Base de Datos

### Esquema de Room

```kotlin
// UniTaskDatabase.kt
@Database(
    entities = [
        TaskEntity::class,
        SubjectEntity::class,
        UserEntity::class,
        UserStatsEntity::class,
        AlarmTemplateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class UniTaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val subjectDao: SubjectDao
    abstract val userDao: UserDao
}
```

### Entidades

**TaskEntity (Tareas):**
```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectId: String,
    val userId: String,
    val dueDateTimeMillis: Long,
    val isCompleted: Boolean
)
```

**UserEntity (Usuarios):**
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val profileImagePath: String?,
    val totalXp: Int,
    val createdAt: Long
)
```

### Migraciones

```kotlin
// Migración de versión 3 a 4 (agregar userId a tasks)
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear tabla temporal
        db.execSQL("""
            CREATE TABLE tasks_new (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                subjectId TEXT NOT NULL,
                userId TEXT NOT NULL DEFAULT '',
                dueDateTimeMillis INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL
            )
        """)
        
        // Copiar datos
        db.execSQL("""
            INSERT INTO tasks_new SELECT id, title, subjectId, '', 
            dueDateTimeMillis, isCompleted FROM tasks
        """)
        
        // Reemplazar tabla
        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
    }
}
```

---

## Sistema de Notificaciones

### Permisos Requeridos

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### AlarmReceiver

```kotlin
// AlarmReceiver.kt - Receptor de alarmas del sistema
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Verificar si es reinicio del sistema
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reprogramar todas las alarmas
            val req = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
            return
        }

        // Obtener datos de la tarea
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val taskTitle = intent.getStringExtra("task_title") ?: "Tarea pendiente"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        
        // Mostrar notificación
        val helper = NotificationHelper(context, nm)
        helper.showReminderNotification(
            id = alarmId,
            title = "📚 Recordatorio: $taskTitle",
            body = "Materia: $subjectName\n¡No olvides completar esta tarea!"
        )
        
        // Vibrar dispositivo
        vibrateDevice(context)
    }
}
```

### Programación de Alarmas

```kotlin
// AlarmScheduler.kt
class AlarmScheduler(private val alarmManagerWrapper: AlarmManagerWrapper) {
    
    fun scheduleExact(
        id: String, 
        triggerAtMillis: Long, 
        repeatIntervalMillis: Long?, 
        intent: PendingIntent
    ) {
        if (repeatIntervalMillis == null) {
            // Alarma única exacta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManagerWrapper.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, 
                    triggerAtMillis, 
                    intent
                )
            } else {
                alarmManagerWrapper.setExact(
                    AlarmManager.RTC_WAKEUP, 
                    triggerAtMillis, 
                    intent
                )
            }
        } else {
            // Alarma repetitiva
            alarmManagerWrapper.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, 
                triggerAtMillis, 
                repeatIntervalMillis, 
                intent
            )
        }
    }
}
```

---

## Sistema de Recompensas

### Cálculo de Nivel

```kotlin
// ProfileViewModel.kt
private fun calculateLevel(xp: Int): Int {
    var level = 1
    var requiredXp = 100
    var totalRequired = requiredXp
    
    while (xp >= totalRequired) {
        level++
        requiredXp = level * 100  // Cada nivel requiere más XP
        totalRequired += requiredXp
    }
    
    return level
}

// Ejemplo:
// Nivel 1: 0-99 XP (necesita 100 XP)
// Nivel 2: 100-299 XP (necesita 200 XP)
// Nivel 3: 300-599 XP (necesita 300 XP)
// etc.
```

### Completar Tarea y Ganar XP

```kotlin
// DashboardViewModel.kt
fun onTaskCompleted(taskId: String) {
    viewModelScope.launch {
        runCatching { completeTaskUseCase(taskId) }
            .onSuccess {
                currentUserId?.let { userId ->
                    // Incrementar tareas completadas
                    userRepository.incrementTasksCompleted(userId)
                    
                    // Agregar 25 XP
                    userRepository.addXp(userId, 25)
                    
                    // Actualizar racha
                    val stats = userRepository.getUserStats(userId)
                    _uiState.update { 
                        it.copy(currentStreak = stats?.currentStreak ?: 0) 
                    }
                }
            }
    }
}
```

---

## Iconos y Recursos

### Ubicación de Iconos

```
app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.webp          # 48x48 px
│   └── ic_launcher_round.webp
├── mipmap-hdpi/
│   ├── ic_launcher.webp          # 72x72 px
│   └── ic_launcher_round.webp
├── mipmap-xhdpi/
│   ├── ic_launcher.webp          # 96x96 px
│   └── ic_launcher_round.webp
├── mipmap-xxhdpi/
│   ├── ic_launcher.webp          # 144x144 px
│   └── ic_launcher_round.webp
├── mipmap-xxxhdpi/
│   ├── ic_launcher.webp          # 192x192 px
│   └── ic_launcher_round.webp
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml           # Icono adaptativo
    └── ic_launcher_round.xml
```

### Iconos Material Design

La app utiliza `Icons.Default` y `Icons.Filled` de Material Icons Extended:

| Icono | Uso |
|-------|-----|
| `Home` | Navegación - Inicio |
| `MenuBook` | Navegación - Materias |
| `Add` | Crear nueva tarea |
| `Person` | Navegación - Perfil |
| `EmojiEvents` | Navegación - Ranking |
| `LocalFireDepartment` | Indicador de racha |
| `Star` | Nivel del usuario |
| `TaskAlt` | Tareas completadas |
| `Refresh` | Actualizar datos |
| `Brightness4/7` | Cambiar tema |

---

## Proceso de Construcción

### Paso 1: Configurar el Entorno

1. Instalar Android Studio (versión recomendada: Ladybug o superior)
2. Configurar el SDK de Android (API 24 mínimo, API 36 target)
3. Clonar o abrir el proyecto

### Paso 2: Sincronizar Dependencias

```bash
# En Android Studio:
# File > Sync Project with Gradle Files

# O desde terminal:
./gradlew build
```

### Paso 3: Compilar Debug APK

```bash
# Desde Android Studio:
# Build > Build Bundle(s) / APK(s) > Build APK(s)

# O desde terminal:
./gradlew assembleDebug
```

**Ubicación del APK Debug:**
```
app/build/outputs/apk/debug/app-debug.apk
```

### Paso 4: Compilar Release APK

```bash
# Desde Android Studio:
# Build > Generate Signed Bundle / APK

# O desde terminal:
./gradlew assembleRelease
```

### Paso 5: Generar App Bundle (Para Play Store)

```bash
# Desde Android Studio:
# Build > Generate Signed Bundle / APK > Android App Bundle

# O desde terminal:
./gradlew bundleRelease
```

**Ubicación del Bundle:**
```
app/build/outputs/bundle/release/app-release.aab
```

### Configuración de Firma (Keystore)

```kotlin
// build.gradle.kts (app)
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "your_store_password"
            keyAlias = "your_key_alias"
            keyPassword = "your_key_password"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## Publicación en Play Store

### Paso 1: Crear Cuenta de Desarrollador

1. Ir a [Google Play Console](https://play.google.com/console)
2. Pagar la tarifa única de $25 USD
3. Completar el registro con datos personales/empresa

### Paso 2: Crear Nueva Aplicación

1. En Play Console: **Crear aplicación**
2. Seleccionar idioma por defecto: **Español**
3. Nombre de la app: **UniTask**
4. Tipo: **Aplicación**
5. Categoría: **Educación** > **Productividad**

### Paso 3: Configurar Ficha de la Tienda

#### Información Básica
| Campo | Valor |
|-------|-------|
| Nombre de la app | UniTask |
| Descripción breve | Organiza tus tareas universitarias y compite con otros estudiantes |
| Descripción completa | (Ver sección "Descripción Larga" arriba) |

#### Recursos Gráficos Requeridos
| Recurso | Dimensiones | Formato |
|---------|-------------|---------|
| Icono de la app | 512 x 512 px | PNG (32-bit) |
| Gráfico de funciones | 1024 x 500 px | PNG/JPG |
| Capturas de pantalla (teléfono) | Mín. 320px, máx. 3840px | PNG/JPG |
| Capturas de pantalla (tablet 7") | Opcional | PNG/JPG |
| Capturas de pantalla (tablet 10") | Opcional | PNG/JPG |

### Paso 4: Configurar Detalles de la Aplicación

#### Categorización de Contenido
1. Ir a **Política** > **Clasificación de contenido**
2. Completar cuestionario de IARC
3. Categoría esperada: **PEGI 3** / **Everyone**

#### Configuración de la App
- **Anuncios**: No contiene anuncios
- **Target de edad**: 13+ años
- **Disponibilidad**: Todos los países

### Paso 5: Subir el App Bundle

1. Ir a **Producción** > **Versiones**
2. Click en **Crear nueva versión**
3. Subir archivo `.aab` generado
4. Agregar notas de la versión:

```
Versión 1.0 - Lanzamiento inicial

✨ Novedades:
• Gestión completa de tareas y asignaturas
• Sistema de alarmas y recordatorios
• Sistema de recompensas y ranking
• Tema claro y oscuro
• Interfaz moderna con Material Design 3

📱 Requisitos:
• Android 7.0 o superior
```

### Paso 6: Revisión y Publicación

1. Completar todas las secciones obligatorias
2. Verificar que no haya errores en el dashboard
3. Click en **Enviar para revisión**
4. Tiempo de revisión: 1-7 días aproximadamente

### Checklist Pre-publicación

- [ ] Icono de 512x512 px subido
- [ ] Gráfico de funciones 1024x500 px subido
- [ ] Mínimo 2 capturas de pantalla de teléfono
- [ ] Descripción breve (80 caracteres máx)
- [ ] Descripción completa (4000 caracteres máx)
- [ ] Clasificación de contenido completada
- [ ] Política de privacidad URL (si recopila datos)
- [ ] App Bundle firmado subido
- [ ] Notas de la versión escritas

---

## Información Adicional

### Versiones y Compatibilidad

| Versión | Código | Fecha | Notas |
|---------|--------|-------|-------|
| 1.0 | 1 | 2026 | Lanzamiento inicial |

### Soporte

- **Email**: soporte@unitask.app
- **Sitio web**: https://unitask.app

### Licencia

Este proyecto es propiedad de [Tu Nombre/Empresa].
Todos los derechos reservados.

---

*Documento generado el 23 de enero de 2026*
*UniTask v1.0*
