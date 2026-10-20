package com.example.pendichell.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Pendichell 🌸"
        val desc = intent.getStringExtra("EXTRA_DESC") ?: "¡Tu actividad pendiente está por vencer! 💕"
        val taskId = intent.getLongExtra("EXTRA_TASK_ID", 0L)

        val channelId = "channel_pendichell_alarm_v3"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Sonido de alarma o notificación del sistema
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Alertas y Alarmas de Pendichell ⏰🌸",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones y alarmas con sonido para tus pendientes"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ ¡Recordatorio Pendichell!: $title")
            .setContentText(desc)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }
}
