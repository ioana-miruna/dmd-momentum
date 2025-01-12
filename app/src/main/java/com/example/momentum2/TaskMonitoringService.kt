package com.example.momentum2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class TaskMonitoringService : Service() {

    private val TAG = "TaskMonitoringService"
    private val binder = LocalBinder()
    private var isMonitoring = false
    private var startTime: Long = 0
    private val handler = Handler()

    private val CHANNEL_ID = "TaskMonitoringChannel"
    private val NOTIFICATION_ID = 1

    // Local Binder class for clients
    inner class LocalBinder : Binder() {
        fun getService(): TaskMonitoringService = this@TaskMonitoringService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Restore state if the service restarts
        val sharedPreferences = getSharedPreferences("TaskMonitoringPrefs", MODE_PRIVATE)
        isMonitoring = sharedPreferences.getBoolean("isMonitoring", false)
        startTime = sharedPreferences.getLong("startTime", 0L)

        if (isMonitoring) {
            startForeground(NOTIFICATION_ID, createNotification(0))
            updateNotification()
            Log.d(TAG, "Service restarted. Resuming monitoring from previous state.")
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound.")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound.")
        return super.onUnbind(intent)
    }

    fun startMonitoringTask() {
        if (isMonitoring) {
            Log.d(TAG, "Monitoring already started.")
            return
        }

        isMonitoring = true
        startTime = System.currentTimeMillis()

        // Save state to SharedPreferences
        val sharedPreferences = getSharedPreferences("TaskMonitoringPrefs", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isMonitoring", true)
            putLong("startTime", startTime)
            apply()
        }

        Log.d(TAG, "Monitoring task started.")
        startForeground(NOTIFICATION_ID, createNotification(0))
        updateNotification()
    }

    fun stopMonitoring() {
        if (!isMonitoring) {
            Log.d(TAG, "No monitoring task to stop.")
            return
        }

        isMonitoring = false
        val elapsedTime = System.currentTimeMillis() - startTime

        // Clear saved state from SharedPreferences
        val sharedPreferences = getSharedPreferences("TaskMonitoringPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Log.d(TAG, "Monitoring task stopped. Elapsed time: ${elapsedTime / 1000} seconds.")
        stopForeground(true)
        stopSelf()
        handler.removeCallbacksAndMessages(null)
    }

    fun getElapsedTime(): Long {
        return if (isMonitoring) {
            (System.currentTimeMillis() - startTime) / 1000
        } else {
            0
        }
    }

    private fun createNotification(elapsedSeconds: Long): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task Monitoring Service")
            .setContentText("Elapsed time: $elapsedSeconds seconds")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                    val notification = createNotification(elapsedSeconds)
                    val notificationManager =
                        getSystemService(NotificationManager::class.java)
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }, 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Monitoring Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
