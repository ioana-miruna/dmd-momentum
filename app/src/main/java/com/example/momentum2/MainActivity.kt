package com.example.momentum2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {

    private lateinit var tvElapsedTime: TextView
    private var taskMonitoringService: TaskMonitoringService? = null
    private var isServiceBound = false
    private val handler = android.os.Handler()
    private lateinit var btnSendBroadcast: Button


    private val updateElapsedTimeRunnable = object : Runnable {
        override fun run() {
            if (isServiceBound) {
                val elapsedTime = taskMonitoringService?.getElapsedTime() ?: 0
                tvElapsedTime.text = "Elapsed Time: ${elapsedTime}s"
            }
            handler.postDelayed(this, 1000) // Update every 1 second
        }
    }

    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isAirplaneModeOn = intent?.getBooleanExtra("state", false) ?: return
            if (isAirplaneModeOn) {
                Log.d("AirplaneModeReceiver", "Airplane Mode is ON")
                btnSendBroadcast.isEnabled = false // Disable button
            } else {
                Log.d("AirplaneModeReceiver", "Airplane Mode is OFF")
                btnSendBroadcast.isEnabled = true // Enable button
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as TaskMonitoringService.LocalBinder
            taskMonitoringService = localBinder.getService()
            isServiceBound = true
            Log.d("MainActivity", "Service connected.")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            taskMonitoringService = null
            isServiceBound = false
            Log.d("MainActivity", "Service disconnected.")
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Notification permission is required for foreground service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            val intent = Intent(this, ViewExpensesActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnResetMonthly).setOnClickListener {
            val sharedPreferences = getSharedPreferences("ExpensesData", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply() // Clear all data
            Toast.makeText(this, "All expenses reset!", Toast.LENGTH_SHORT).show()
        }

        val serviceIntent = Intent(this, BackgroundCounterService::class.java)
        startForegroundService(serviceIntent)

        findViewById<ImageButton>(R.id.info_button_main).setOnClickListener {
            Toast.makeText(
                this,
                "You will receive real-life updates on your budget.",
                Toast.LENGTH_LONG
            ).show()
        }


        btnSendBroadcast = findViewById(R.id.btnSendBroadcast)
        val isAirplaneModeOn = android.provider.Settings.Global.getInt(
            contentResolver,
            android.provider.Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0

        btnSendBroadcast.isEnabled = !isAirplaneModeOn

        btnSendBroadcast.setOnClickListener {
            val intent = Intent("com.example.momentum2.MY_CUSTOM_ACTION")
            intent.setPackage(packageName) // Ensure the broadcast is explicit
            intent.putExtra("message", "Hello from explicit broadcast!")
            sendBroadcast(intent)
            Toast.makeText(this, "Broadcast sent!", Toast.LENGTH_SHORT).show()
        }

        tvElapsedTime = findViewById(R.id.tvElapsedTime)

        findViewById<Button>(R.id.btn_start_budget_tracker).setOnClickListener {
            val intent = Intent(this, BudgetTrackerService::class.java)
            startService(intent)
        }

        setDailyAlarms()

        val intent = Intent(this, TaskMonitoringService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        findViewById<Button>(R.id.btnStartMonitoring).setOnClickListener {
            if (isServiceBound) {
                taskMonitoringService?.startMonitoringTask()
                Toast.makeText(this, "Monitoring started.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service not bound.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnStopMonitoring).setOnClickListener {
            if (isServiceBound) {
                taskMonitoringService?.stopMonitoring()
                Toast.makeText(this, "Monitoring stopped.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service not bound.", Toast.LENGTH_SHORT).show()
            }
        }

        handler.post(updateElapsedTimeRunnable)
    }

    private fun setDailyAlarms() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        setAlarm(alarmManager, 10, 0)
        setAlarm(alarmManager, 16, 0)
        setAlarm(alarmManager, 20, 0)
    }

    private fun setAlarm(alarmManager: AlarmManager, hour: Int, minute: Int) {
        val intent = Intent(this, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm for the specified hour and minute
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Set a repeating alarm (daily)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onStart() {
        super.onStart()
        // Start BackgroundCounterService in foreground mode to ensure it runs independently
        val serviceIntent = Intent(this, BackgroundCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onStop() {
        super.onStop()

        val serviceIntent = Intent(this, BackgroundCounterService::class.java)
        stopService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(updateElapsedTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(airplaneModeReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(airplaneModeReceiver)
    }

}
