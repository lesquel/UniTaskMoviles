package com.example.unitask.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Datos de cada página del onboarding.
 */
data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tips: List<String>,
    val backgroundColor: Color
)

/**
 * Páginas del tutorial de onboarding.
 */
private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.Home,
        title = "¡Bienvenido a UniTask!",
        description = "Tu asistente personal para gestionar tareas universitarias",
        tips = listOf(
            "Organiza todas tus tareas en un solo lugar",
            "Recibe recordatorios para no olvidar entregas",
            "Gana XP y sube de nivel completando tareas"
        ),
        backgroundColor = Color(0xFF6750A4)
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Materias",
        description = "Organiza tus tareas por asignaturas",
        tips = listOf(
            "Crea materias con colores personalizados",
            "Asigna cada tarea a una materia",
            "Filtra tareas por materia fácilmente"
        ),
        backgroundColor = Color(0xFF1976D2)
    ),
    OnboardingPage(
        icon = Icons.Default.Add,
        title = "Agregar Tareas",
        description = "Crea y gestiona tus tareas fácilmente",
        tips = listOf(
            "Toca el botón + para crear una nueva tarea",
            "Selecciona la fecha de entrega",
            "Asigna alarmas para recibir recordatorios"
        ),
        backgroundColor = Color(0xFF388E3C)
    ),
    OnboardingPage(
        icon = Icons.Default.SwipeRight,
        title = "Gestos Rápidos",
        description = "Desliza las tareas para acciones rápidas",
        tips = listOf(
            "Desliza a la derecha para completar ✓",
            "Desliza a la izquierda para eliminar 🗑️",
            "Toca una tarea para ver o editar detalles"
        ),
        backgroundColor = Color(0xFFE65100)
    ),
    OnboardingPage(
        icon = Icons.Default.Notifications,
        title = "Alarmas y Recordatorios",
        description = "Nunca olvides una entrega importante",
        tips = listOf(
            "Configura alarmas para cada tarea",
            "Recibe notificaciones aunque la app esté cerrada",
            "Usa plantillas de alarma para configurar rápido"
        ),
        backgroundColor = Color(0xFFD81B60)
    ),
    OnboardingPage(
        icon = Icons.Default.EmojiEvents,
        title = "Sistema de Recompensas",
        description = "¡Gana XP y compite con otros estudiantes!",
        tips = listOf(
            "Gana 25 XP por cada tarea completada",
            "Sube de nivel acumulando experiencia",
            "Mantén tu racha completando tareas diarias"
        ),
        backgroundColor = Color(0xFF00796B)
    ),
    OnboardingPage(
        icon = Icons.Default.Leaderboard,
        title = "Leaderboard",
        description = "Compara tu progreso con otros usuarios",
        tips = listOf(
            "Mira el ranking de los mejores estudiantes",
            "Compite por el primer lugar",
            "Ve cuántas tareas han completado otros"
        ),
        backgroundColor = Color(0xFF303F9F)
    )
)

/**
 * Pantalla de onboarding/tutorial para nuevos usuarios.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        onboardingPages[pagerState.currentPage].backgroundColor,
                        onboardingPages[pagerState.currentPage].backgroundColor.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Botón de saltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = "Saltar",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Contenido del pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(page = onboardingPages[page])
            }
            
            // Indicadores y botones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicadores de página
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
                
                // Botones de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón Anterior
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Anterior")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    // Botón Siguiente o Empezar
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = onboardingPages[pagerState.currentPage].backgroundColor
                            )
                        ) {
                            Text("Siguiente")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = onboardingPages[pagerState.currentPage].backgroundColor
                            )
                        ) {
                            Text("¡Empezar!", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono grande
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Título
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Descripción
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Card con tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                page.tips.forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }
    }
}
