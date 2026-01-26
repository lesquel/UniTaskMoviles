package com.example.unitask.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Representa una opción de filtro de día.
 */
sealed class DayFilter {
    object All : DayFilter()
    data class SpecificDay(val dayOfWeek: DayOfWeek) : DayFilter()
    
    fun getDisplayName(): String {
        return when (this) {
            is All -> "Todos"
            is SpecificDay -> dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES"))
                .replaceFirstChar { it.uppercase() }
        }
    }
}

/**
 * Lista de todos los filtros disponibles.
 */
val allDayFilters: List<DayFilter> = listOf(DayFilter.All) + DayOfWeek.entries.map { DayFilter.SpecificDay(it) }

/**
 * Chips horizontales para filtrar tareas por día de la semana.
 */
@Composable
fun DayFilterChips(
    selectedFilter: DayFilter,
    onFilterSelected: (DayFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allDayFilters) { filter ->
            val isSelected = when {
                filter is DayFilter.All && selectedFilter is DayFilter.All -> true
                filter is DayFilter.SpecificDay && selectedFilter is DayFilter.SpecificDay -> 
                    filter.dayOfWeek == selectedFilter.dayOfWeek
                else -> false
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.getDisplayName()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
