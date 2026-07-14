package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.data.models.HapticLog
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object HapticHelper {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun triggerAndLog(
        context: Context,
        eventType: String,
        details: String,
        userEmail: String? = null
    ) {
        // 1. Trigger vibration
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 100)) // 20 milliseconds, subtle 100/255 amplitude
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Log to Supabase background coroutine
        scope.launch {
            try {
                val email = userEmail ?: "anonymous"
                val log = HapticLog(
                    id = UUID.randomUUID().toString(),
                    event_type = eventType,
                    user_email = email,
                    details = details,
                    created_at = System.currentTimeMillis()
                )
                SupabaseService.client.from("haptic_logs").insert(log)
            } catch (e: Exception) {
                // Fail gracefully: table might not exist yet or there may be no network connection
                e.printStackTrace()
            }
        }
    }
}
