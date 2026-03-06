package com.twintipsolutions.guthealth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.TimeZone

class GutHealthMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveFCMToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "AI Gut Health",
                body = notification.body ?: "Time to log your meal!"
            )
        }
    }

    private fun saveFCMToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val timezone = TimeZone.getDefault().id
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update(
                mapOf(
                    "fcmToken" to token,
                    "preferences.timezone" to timezone
                )
            )
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "gut_health_reminders"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Meal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log your meals and symptoms"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
