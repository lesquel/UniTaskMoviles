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

En esta actualización del proyecto, se han aplicado y mejorado los siguientes aspectos:

### Identificación de 3 buenas prácticas preexistentes

1.  **Arquitectura Limpia (Clean Architecture):** El proyecto ya separaba claramente `Domain` (UseCases), `Data` (Repositories) y `Presentation` (ViewModels).
    - _Evidencia:_ `AddTaskUseCase` encapsula la lógica de creación.
2.  **State Hoisting:** Separación entre `DashboardRoute` (con lógica) y `DashboardScreen` (puramente visual).
    - _Ubicación:_ `presentation/ui/screens/DashboardScreen.kt`.
3.  **Inyección de Dependencias Manual:** Uso de `AppModule` para proveer instancias únicas de repositorios y ViewModels, facilitando el testing.

### Identificación de 2 aspectos mejorados (Implementados)

Se detectaron áreas de mejora crítica y se refactorizaron:

#### 1. Persistencia Real con Room (Optimización de Datos)

- **Antes:** Se usaba `InMemoryTaskRepository`, perdiendo los datos al cerrar la app.
- **Mejora:** Se implementó `UniTaskDatabase`, `TaskDao`, `SubjectDao` y entidades (`TaskEntity`).
- **Código modificado:**
  - Creación de `data/room/UniTaskDatabase.kt`.
  - Refactorización de `di/AppModule.kt` para inicializar la base de datos.

#### 2. Recolección de Estado Consciente del Ciclo de Vida (Optimización de UI)

- **Antes:** Se usaba `collectAsState()` en `DashboardRoute`. Esto mantenía la suscripción al flujo activa incluso si la app estaba en "Stop" (segundo plano), desperdiciando recursos.
- **Mejora:** Cambio a `collectAsStateWithLifecycle()`.
- **Beneficio:** La UI deja de procesar actualizaciones cuando no es visible, ahorrando batería.
- **Ubicación:** `presentation/ui/screens/DashboardScreen.kt`.

#### 3. Extracción de Strings y Manejo de Errores (Calidad de Código)

- **Antes:** `AddTaskViewModel` contenía strings "hardcoded" (`"El título es requerido"`).
- **Mejora:** Se creó una interfaz `AddTaskError` y se movieron los textos a `strings.xml`.
- **Beneficio:** Soporte para traducción (i18n) y separación de lógica (ViewModel) de la presentación (Texto UI).

---

## 6. Conclusión

La aplicación de estas buenas prácticas transforma UniTask de un prototipo funcional a una aplicación robusta y profesional. La introducción de persistencia (Room) garantiza que el usuario no pierda trabajo, mientras que las optimizaciones de UI (Lifecycle) y código (Strings) aseguran que la app sea mantenible y eficiente energéticamente. Publicar una app sin estos estándares resultaría en malas reseñas por pérdida de datos o consumo excesivo de batería.
