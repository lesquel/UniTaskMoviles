package com.example.unitask.domain.model

// Modelo ligero que agrupa XP, nivel y marca temporal del último premio.
data class Reward(
    val xp: Int,                // Experiencia acumulada.
    val level: Int,             // Nivel actual basado en XP.
    val lastAwardedAt: Long     // Epoch del último otorgamiento.
)
