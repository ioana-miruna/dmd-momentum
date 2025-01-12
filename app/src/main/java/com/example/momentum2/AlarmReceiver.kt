package com.example.momentum2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Log to check if alarm went off
        Log.d("AlarmReceiver", "Alarm received. Starting service.")

        // Start the BudgetTrackerService to show the notification
        val serviceIntent = Intent(context, BudgetTrackerService::class.java)
        context.startService(serviceIntent)
    }
}
