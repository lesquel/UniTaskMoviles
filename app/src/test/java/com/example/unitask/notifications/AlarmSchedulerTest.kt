package com.example.unitask.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class AlarmSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val wrapper = FakeAlarmManagerWrapper()
    private val scheduler = AlarmScheduler(wrapper)

    private val intent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @Test
    fun `schedules exact alarm using allow while idle`() {
        scheduler.scheduleExact("once", System.currentTimeMillis() + 1000L, null, intent)

        val call = wrapper.calls.firstOrNull { it.type == FakeAlarmManagerWrapper.CallType.EXACT_IDLE }
        assertTrue(call != null)
        assertEquals(FakeAlarmManagerWrapper.CallType.EXACT_IDLE, call?.type)
    }

    @Test
    fun `schedules repeating alarm with clamped interval`() {
        scheduler.scheduleExact("repeat", System.currentTimeMillis() + 2000L, 30_000L, intent)
        val call = wrapper.calls.firstOrNull { it.type == FakeAlarmManagerWrapper.CallType.REPEAT }
        assertTrue(call != null)
        assertEquals(60_000L, call?.interval)
    }

    private class FakeAlarmManagerWrapper : AlarmManagerWrapper {
        val calls = mutableListOf<Call>()

        override fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
            calls += Call(CallType.EXACT_IDLE, triggerAtMillis)
        }

        override fun setExact(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
            calls += Call(CallType.EXACT, triggerAtMillis)
        }

        override fun setInexactRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long, intent: PendingIntent) {
            calls += Call(CallType.REPEAT, triggerAtMillis, intervalMillis)
        }

        override fun cancel(intent: PendingIntent) {
            calls += Call(CallType.CANCEL, triggerAtMillis = 0)
        }

        data class Call(val type: CallType, val triggerAtMillis: Long, val interval: Long? = null)

        enum class CallType {
            EXACT_IDLE,
            EXACT,
            REPEAT,
            CANCEL
        }
    }
}
