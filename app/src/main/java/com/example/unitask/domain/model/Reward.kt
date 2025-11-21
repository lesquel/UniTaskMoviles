package com.example.unitask.domain.model

// Modelo ligero para la experiencia acumulada y nivel del usuario.

data class Reward(
    val xp: Int,
    val level: Int,
    val lastAwardedAt: Long
)
