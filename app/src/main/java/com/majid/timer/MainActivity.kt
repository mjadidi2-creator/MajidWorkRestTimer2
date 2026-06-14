package com.majid.timer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var workInput: EditText
    private lateinit var restInput: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(36, 36, 36, 36)
        }

        val title = TextView(this).apply {
            text = "Majid Work / Rest Timer"
            textSize = 22f
            gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        workInput = EditText(this).apply {
            hint = "زمان کار به دقیقه، مثلا 45"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("45")
        }
        restInput = EditText(this).apply {
            hint = "زمان استراحت به دقیقه، مثلا 10"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("10")
        }
        root.addView(workInput, LinearLayout.LayoutParams(-1, -2))
        root.addView(restInput, LinearLayout.LayoutParams(-1, -2))

        val start = Button(this).apply {
            text = "Start"
            setOnClickListener { startTimer() }
        }
        val stop = Button(this).apply {
            text = "Stop"
            setOnClickListener {
                stopService(Intent(this@MainActivity, TimerOverlayService::class.java))
                status.text = "تایمر متوقف شد."
            }
        }
        root.addView(start, LinearLayout.LayoutParams(-1, -2))
        root.addView(stop, LinearLayout.LayoutParams(-1, -2))

        status = TextView(this).apply {
            text = "برای نمایش تایمر کوچک، اجازه Draw over other apps لازم است."
            textSize = 14f
            gravity = Gravity.CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private fun startTimer() {
        val work = workInput.text.toString().toIntOrNull() ?: 45
        val rest = restInput.text.toString().toIntOrNull() ?: 10

        if (work !in 1..1440 || rest !in 1..1440) {
            status.text = "هر زمان باید بین 1 دقیقه تا 24 ساعت باشد."
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            status.text = "اول اجازه نمایش روی برنامه‌های دیگر را فعال کن، بعد دوباره Start را بزن."
            return
        }

        val intent = Intent(this, TimerOverlayService::class.java).apply {
            putExtra("workMinutes", work)
            putExtra("restMinutes", rest)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        moveTaskToBack(true)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }
}
