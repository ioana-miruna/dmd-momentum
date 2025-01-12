package com.example.momentum2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class BackgroundCounterService : Service() {

    private val TAG = "BackgroundCounterService"
    private var isCounting = false
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L

    private val CHANNEL_ID = "BackgroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isCounting) {
            isCounting = true
            startTime = System.currentTimeMillis() - elapsedTime

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
                val notification = Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("App Timer")
                    .setContentText("Tracking time spent in the app.")
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .build()
                startForeground(1, notification)
            }

            Thread {
                countElapsedTime()
            }.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isCounting = false
        elapsedTime += System.currentTimeMillis() - startTime
        Log.d(TAG, "Service destroyed. Total time spent in the app: ${elapsedTime / 1000} seconds.")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val totalTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "App closed. Total time spent: ${totalTime / 1000} seconds.")
        stopSelf() // Stop the service when the app is closed
        super.onTaskRemoved(rootIntent)
    }

    private fun countElapsedTime() {
        while (isCounting) {
            val currentElapsedTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Time spent in app: ${currentElapsedTime / 1000} seconds")

            try {
                Thread.sleep(1000) // Wait for 1 second
            } catch (e: InterruptedException) {
                Log.e(TAG, "Counting thread interrupted", e)
                break
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
