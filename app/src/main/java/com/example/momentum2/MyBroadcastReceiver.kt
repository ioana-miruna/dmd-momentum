package com.example.momentum2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: "No message received"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

