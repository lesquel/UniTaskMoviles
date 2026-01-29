package com.example.unitask.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Efecto de shimmer animado para los skeleton loaders.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.surface
): Brush {
    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 200f, translateAnimation - 200f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Skeleton básico con forma rectangular.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    cornerRadius: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush())
    )
}

/**
 * Skeleton circular para avatares.
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

/**
 * Skeleton para una tarjeta de tarea.
 */
@Composable
fun TaskCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título
            SkeletonBox(
                width = 200.dp,
                height = 24.dp,
                cornerRadius = 4.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Materia
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonCircle(size = 12.dp)
                Spacer(modifier = Modifier.width(8.dp))
                SkeletonBox(
                    width = 120.dp,
                    height = 16.dp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fecha
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    width = 16.dp,
                    height = 16.dp,
                    cornerRadius = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                SkeletonBox(
                    width = 150.dp,
                    height = 14.dp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón
            SkeletonBox(
                modifier = Modifier.align(Alignment.End),
                width = 100.dp,
                height = 36.dp,
                cornerRadius = 18.dp
            )
        }
    }
}

/**
 * Skeleton para una tarjeta de materia.
 */
@Composable
fun SubjectCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            SkeletonCircle(size = 24.dp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre
                SkeletonBox(
                    width = 150.dp,
                    height = 20.dp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Profesor
                SkeletonBox(
                    width = 100.dp,
                    height = 14.dp
                )
            }
            
            // Botones de acción
            Row {
                SkeletonCircle(size = 32.dp)
                Spacer(modifier = Modifier.width(8.dp))
                SkeletonCircle(size = 32.dp)
            }
        }
    }
}

/**
 * Skeleton para un item del leaderboard.
 */
@Composable
fun LeaderboardItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            SkeletonBox(
                width = 32.dp,
                height = 32.dp,
                cornerRadius = 16.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Avatar
            SkeletonCircle(size = 48.dp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Username
                SkeletonBox(
                    width = 120.dp,
                    height = 18.dp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // XP y tareas
                Row {
                    SkeletonBox(
                        width = 60.dp,
                        height = 14.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeletonBox(
                        width = 60.dp,
                        height = 14.dp
                    )
                }
            }
            
            // Level badge
            SkeletonBox(
                width = 40.dp,
                height = 24.dp,
                cornerRadius = 12.dp
            )
        }
    }
}

/**
 * Lista de skeletons para tareas.
 */
@Composable
fun TaskListSkeleton(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            TaskCardSkeleton()
        }
    }
}

/**
 * Lista de skeletons para materias.
 */
@Composable
fun SubjectListSkeleton(
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SubjectCardSkeleton()
        }
    }
}

/**
 * Lista de skeletons para leaderboard.
 */
@Composable
fun LeaderboardListSkeleton(
    itemCount: Int = 10,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            LeaderboardItemSkeleton()
        }
    }
}

/**
 * Skeleton para la sección de tareas urgentes (horizontal).
 */
@Composable
fun UrgentTasksSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.width(260.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    SkeletonBox(width = 180.dp, height = 20.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBox(width = 120.dp, height = 14.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBox(width = 140.dp, height = 14.dp)
                }
            }
        }
    }
}

/**
 * Skeleton para el perfil del usuario.
 */
@Composable
fun ProfileSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        SkeletonCircle(size = 120.dp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Username
        SkeletonBox(width = 150.dp, height = 28.dp)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Email
        SkeletonBox(width = 200.dp, height = 18.dp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SkeletonBox(width = 50.dp, height = 28.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonBox(width = 70.dp, height = 14.dp)
                    }
                }
            }
        }
    }
}
