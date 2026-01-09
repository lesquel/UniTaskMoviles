# Subtema 4: Pruebas de Interfaz (UI Testing) en UniTask

## 📚 ¿Qué son las Pruebas de Interfaz?

Las **pruebas de interfaz de usuario (UI Testing)** son tests automatizados que validan el comportamiento visual e interactivo de una aplicación desde la perspectiva del usuario. A diferencia de las pruebas unitarias que evalúan funciones aisladas, las pruebas de UI simulan cómo un usuario real interactúa con la app.

### Características principales:
- **Ejecutan en un dispositivo real o emulador**: Necesitan el contexto completo de Android
- **Simulan interacciones de usuario**: Clics, escritura, scrolling, gestos
- **Validan elementos visuales**: Textos, botones, imágenes, estados
- **Prueban navegación**: Transiciones entre pantallas

## 🎯 ¿Qué Validan las Pruebas de UI?

### 1. **Botones y Acciones**
```kotlin
// Verificar que un botón existe y es clickeable
composeRule.onNodeWithContentDescription("Agregar tarea")
    .assertIsDisplayed()
    .performClick()
```

### 2. **Textos y Labels**
```kotlin
// Verificar que un texto se muestra correctamente
composeRule.onNodeWithText("Nueva Tarea").assertIsDisplayed()
```

### 3. **Navegación**
```kotlin
// Verificar navegación después de un clic
composeRule.onNodeWithText("Agregar").performClick()
composeRule.onNodeWithText("Crear Tarea").assertIsDisplayed()
```

### 4. **Estados de la UI**
```kotlin
// Verificar estado vacío
composeRule.onNodeWithText("No hay tareas pendientes").assertIsDisplayed()
```

### 5. **Formularios**
```kotlin
// Verificar entrada de texto
composeRule.onNodeWithTag("titulo_tarea")
    .performTextInput("Mi nueva tarea")
    .assert(hasText("Mi nueva tarea"))
```

## 📱 Importancia en Apps Móviles

### ¿Por qué son críticas?

| Aspecto | Sin UI Testing | Con UI Testing |
|---------|----------------|----------------|
| Regresiones visuales | No detectadas | Detectadas automáticamente |
| Experiencia de usuario | Incierta | Validada |
| Tiempo de QA manual | Alto | Reducido |
| Confianza en releases | Baja | Alta |
| Navegación rota | Descubierta por usuarios | Descubierta en CI/CD |

### Beneficios específicos para móviles:
1. **Fragmentación de dispositivos**: Validan comportamiento en diferentes tamaños de pantalla
2. **Ciclos de release rápidos**: Permiten releases frecuentes con confianza
3. **Interacciones complejas**: Validan gestos, animaciones, transiciones
4. **Accesibilidad**: Verifican que la app sea usable con content descriptions

## 🏗️ Framework usado: Jetpack Compose Testing

UniTask usa **Jetpack Compose**, por lo que utilizamos el framework de testing nativo de Compose:

```kotlin
dependencies {
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Componentes clave:

| Componente | Descripción |
|------------|-------------|
| `createComposeRule()` | Crea el entorno de testing |
| `onNodeWithText()` | Busca elementos por texto |
| `onNodeWithContentDescription()` | Busca por descripción de accesibilidad |
| `onNodeWithTag()` | Busca por test tag |
| `assertIsDisplayed()` | Verifica visibilidad |
| `performClick()` | Simula clic |
| `performTextInput()` | Escribe texto |

---

## 📋 Aplicación al Proyecto UniTask

### Pantallas Clave que Deben Probarse

#### 1. **DashboardScreen** (Pantalla Principal)
- **Elementos a validar:**
  - Lista de tareas urgentes
  - Lista de todas las tareas
  - Botón flotante para agregar tarea
  - Estado vacío cuando no hay tareas
  - Barra de recompensas (XP y nivel)
  
- **Archivo de test:** `DashboardScreenTest.kt`

#### 2. **AddTaskScreen** (Crear/Editar Tarea)
- **Elementos a validar:**
  - Campo de título
  - Selector de materia
  - Selectores de fecha y hora
  - Botón de guardar
  - Validación de errores
  - Botón de retroceso

- **Archivo de test:** `AddTaskScreenTest.kt`

#### 3. **SubjectsScreen** (Gestión de Materias)
- **Elementos a validar:**
  - Lista de materias
  - FAB para agregar materia
  - Diálogo de creación/edición
  - Confirmación de eliminación
  - Estado vacío

- **Archivo de test:** `SubjectsScreenTest.kt`

#### 4. **Navegación**
- **Flujos a validar:**
  - Dashboard → AddTask
  - Dashboard → Subjects
  - AddTask → Dashboard (back)
  - Subjects → Dashboard (back)

- **Archivo de test:** `NavigationTest.kt`

#### 5. **Componentes Reutilizables**
- **TaskCard:** Tarjeta de tarea con acciones
- **EmptyState:** Estado vacío
- **RewardsBar:** Barra de progreso

- **Archivo de test:** `ComponentsTest.kt`

---

## ⚠️ ¿Qué Pasaría si Fallan? (Ejemplos Reales)

### Escenario 1: FAB de Agregar Tarea No Funciona
```kotlin
@Test
fun fabNotClickable_userCannotCreateTasks() {
    // Si este test falla, el usuario NO puede crear nuevas tareas
    composeRule.onNodeWithContentDescription("Agregar tarea")
        .assertIsDisplayed()
        .performClick()
    
    // Debería navegar a AddTaskScreen
    composeRule.onNodeWithText("Nueva Tarea").assertIsDisplayed()
}
```
**Impacto:** 🔴 CRÍTICO - La funcionalidad principal de la app está rota

### Escenario 2: Validación de Formulario No Aparece
```kotlin
@Test
fun emptyTitle_showsValidationError() {
    // Si falla, usuarios guardan tareas sin título
    composeRule.onNodeWithText("Guardar").performClick()
    composeRule.onNodeWithText("El título es requerido").assertIsDisplayed()
}
```
**Impacto:** 🟠 ALTO - Datos corruptos en la base de datos

### Escenario 3: Estado Vacío No Se Muestra
```kotlin
@Test
fun noTasks_showsEmptyState() {
    // Si falla, usuarios ven una pantalla en blanco confusa
    composeRule.onNodeWithText("No hay tareas").assertIsDisplayed()
}
```
**Impacto:** 🟡 MEDIO - Mala experiencia de usuario, confusión

### Escenario 4: Navegación de Retroceso Rota
```kotlin
@Test
fun backButton_returnsToPresiousScreen() {
    // Si falla, usuarios quedan atrapados en una pantalla
    composeRule.onNodeWithContentDescription("Volver").performClick()
    composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
}
```
**Impacto:** 🔴 CRÍTICO - Usuario debe cerrar la app para salir

---

## 🧪 Estructura de Tests en UniTask

```
app/src/androidTest/java/com/example/unitask/
├── ExampleInstrumentedTest.kt          # Test básico de contexto
└── presentation/
    └── ui/
        ├── screens/
        │   ├── DashboardScreenTest.kt   # Tests de Dashboard
        │   ├── AddTaskScreenTest.kt     # Tests de AddTask
        │   └── SubjectsScreenTest.kt    # Tests de Subjects
        ├── components/
        │   └── ComponentsTest.kt        # Tests de componentes
        └── navigation/
            └── NavigationTest.kt        # Tests de navegación
```

---

## 🚀 Cómo Ejecutar las Pruebas

### Desde Android Studio
1. Click derecho en el paquete `androidTest`
2. Seleccionar "Run Tests in..."

### Desde Terminal
```bash
# Ejecutar todos los tests de UI
./gradlew connectedAndroidTest

# Ejecutar un test específico
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.unitask.presentation.ui.screens.DashboardScreenTest
```

### Requisitos
- Emulador corriendo o dispositivo conectado
- API level 24+ (minSdk del proyecto)

---

## 📊 Métricas de Cobertura

Para verificar qué tanto de la UI está cubierta:

```bash
./gradlew createDebugCoverageReport
```

El reporte se genera en:
`app/build/reports/coverage/androidTest/debug/index.html`

---

## ✅ Best Practices Implementadas

1. **Test Tags para elementos clave**
   ```kotlin
   Modifier.testTag("fab_add_task")
   ```

2. **Descripción de contenido para accesibilidad**
   ```kotlin
   contentDescription = stringResource(R.string.add_task)
   ```

3. **Estados predecibles en tests**
   ```kotlin
   // Usamos estados mock en lugar de ViewModels reales
   DashboardScreen(state = DashboardUiState(...))
   ```

4. **Aislamiento de tests**
   - Cada test es independiente
   - No comparten estado
   - Pueden ejecutarse en cualquier orden

---

## 🔗 Referencias

- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [Testing Cheatsheet](https://developer.android.com/jetpack/compose/testing-cheatsheet)
- [Espresso Documentation](https://developer.android.com/training/testing/espresso)
