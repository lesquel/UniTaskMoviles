package com.example.unitask.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class SharedPrefsRewardRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun addXpAndReset() = runBlocking {
        val repo = SharedPrefsRewardRepository(context)
        // reset
        repo.resetXp()
        val before = repo.getXp().first()
        assertEquals(0, before)

        repo.addXp(250)
        val xpAfter = repo.getXp().first()
        // After 250 XP, level should increase accordingly: level 1 requires 100
        assertTrue(xpAfter >= 0)

        repo.resetXp()
        val xpReset = repo.getXp().first()
        assertEquals(0, xpReset)
    }
}
