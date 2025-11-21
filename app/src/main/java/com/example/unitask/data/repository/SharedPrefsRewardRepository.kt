package com.example.unitask.data.repository

import android.content.Context
import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PREFS_NAME = "unitask_prefs"
private const val KEY_XP = "reward_xp"
private const val KEY_LEVEL = "reward_level"

class SharedPrefsRewardRepository(private val context: Context) : RewardRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _xp = MutableStateFlow(prefs.getInt(KEY_XP, 0))
    private val _level = MutableStateFlow(prefs.getInt(KEY_LEVEL, 1))

    // Suma XP, escala niveles y persiste los valores.
    override suspend fun addXp(amount: Int) {
        val currentXp = _xp.value
        val currentLevel = _level.value
        var newXp = currentXp + amount
        var newLevel = currentLevel
        while (newXp >= newLevel * 100) {
            newXp -= newLevel * 100
            newLevel += 1
        }
        prefs.edit().putInt(KEY_XP, newXp).putInt(KEY_LEVEL, newLevel).apply()
        _xp.value = newXp
        _level.value = newLevel
    }

    // Flujo que expone el XP actual.
    override fun getXp(): Flow<Int> = _xp

    // Reinicia la XP y nivel en 0/1.
    override suspend fun resetXp() {
        prefs.edit().putInt(KEY_XP, 0).putInt(KEY_LEVEL, 1).apply()
        _xp.value = 0
        _level.value = 1
    }

    // Flujo que expone el nivel actual.
    override fun getLevel(): Flow<Int> = _level
}
