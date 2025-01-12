package com.example.momentum2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONArray

class BudgetTrackerService : Service() {

    private val TAG = "ForegroundCounterService"
    private var updatedExpenses = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // define a channel for the notification
        // this is used so that you can group the types of notifications you want to have in your app
        val channelId = "ForegroundServiceChannel"
        val channelName = "Foreground Service"

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        //create a notification
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Daily Expenses Reminder")
            .setContentText("Don't forget to log your expenses today!")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .build()

        //this is when the foreground service actually starts
        startForeground(1, notification)
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        updatedExpenses = false
        Log.d(TAG, "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
