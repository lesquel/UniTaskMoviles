package com.example.unitask.presentation.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.R
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.LeaderboardFilter
import com.example.unitask.presentation.viewmodel.LeaderboardViewModel
import com.example.unitask.presentation.viewmodel.UserRankingItem

/**
 * Pantalla de ranking de usuarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    val viewModel: LeaderboardViewModel = viewModel(factory = AppModule.leaderboardViewModelFactory())
    val state by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.leaderboard_title)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filtros por categoría
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filters) { filter ->
                    FilterChip(
                        selected = filter == state.selectedFilter,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(filter.displayName) }
                    )
                }
            }
            
            if (state.rankings.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.leaderboard_no_users),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Podio para top 3
                    if (state.rankings.size >= 3) {
                        item {
                            PodiumSection(
                                first = state.rankings[0],
                                second = state.rankings[1],
                                third = state.rankings[2]
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Lista completa
                    itemsIndexed(state.rankings) { index, user ->
                        UserRankingCard(
                            position = index + 1,
                            user = user,
                            isCurrentUser = user.userId == state.currentUserId
                        )
                    }
                    
                    // Posición del usuario actual si no está en el top
                    state.currentUserPosition?.let { position ->
                        if (position > state.rankings.size) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.leaderboard_your_position),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                state.currentUserData?.let { userData ->
                                    UserRankingCard(
                                        position = position,
                                        user = userData,
                                        isCurrentUser = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(
    first: UserRankingItem,
    second: UserRankingItem,
    third: UserRankingItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Segundo lugar
        PodiumItem(
            position = 2,
            user = second,
            height = 80.dp,
            color = Color(0xFFC0C0C0) // Plata
        )
        
        // Primer lugar
        PodiumItem(
            position = 1,
            user = first,
            height = 100.dp,
            color = Color(0xFFFFD700) // Oro
        )
        
        // Tercer lugar
        PodiumItem(
            position = 3,
            user = third,
            height = 60.dp,
            color = Color(0xFFCD7F32) // Bronce
        )
    }
}

@Composable
private fun PodiumItem(
    position: Int,
    user: UserRankingItem,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = user.username,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "${user.totalXp} XP",
            style = MaterialTheme.typography.labelSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Pedestal
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UserRankingCard(
    position: Int,
    user: UserRankingItem,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp)
            )
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.leaderboard_tasks_count, user.tasksCompleted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // XP
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.leaderboard_xp_total, user.totalXp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.rewards_level_label, user.level),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
