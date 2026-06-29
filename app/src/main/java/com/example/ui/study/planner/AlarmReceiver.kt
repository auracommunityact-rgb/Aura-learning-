package com.example.ui.study.planner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ALARM_TRIGGERED") {
            val sessionId = intent.getLongExtra("SESSION_ID", -1)
            val subject = intent.getStringExtra("SUBJECT") ?: "Study Time"
            val topic = intent.getStringExtra("TOPIC") ?: "Let's focus"

            Log.d("AlarmReceiver", "Alarm triggered for session $sessionId")

            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("SESSION_ID", sessionId)
                putExtra("SUBJECT", subject)
                putExtra("TOPIC", topic)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                sessionId.toInt(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "study_alarms",
                    "Study Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarms for scheduled study sessions"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, "study_alarms")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(subject)
                .setContentText(topic)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(sessionId.toInt(), notification)
        }
    }
}

