package com.majid.timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class TimerOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var windowManager: WindowManager
    private var overlay: TextView? = null
    private var workMs: Long = 45 * 60 * 1000L
    private var restMs: Long = 10 * 60 * 1000L
    private var remainingMs: Long = workMs
    private var elapsedMs: Long = 0L
    private var isWork = true
    private var lastTick = 0L
    private var tone: ToneGenerator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        workMs = ((intent?.getIntExtra("workMinutes", 45) ?: 45).coerceIn(1, 1440)) * 60 * 1000L
        restMs = ((intent?.getIntExtra("restMinutes", 10) ?: 10).coerceIn(1, 1440)) * 60 * 1000L
        remainingMs = workMs
        elapsedMs = 0L
        isWork = true
        startForeground(1, buildNotification("شروع تایمر"))
        showOverlay()
        lastTick = SystemClock.elapsedRealtime()
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    private val tick = object : Runnable {
        override fun run() {
            val now = SystemClock.elapsedRealtime()
            val diff = now - lastTick
            lastTick = now
            elapsedMs += diff
            remainingMs -= diff

            if (remainingMs <= 0) {
                beep15Seconds()
                isWork = !isWork
                remainingMs = if (isWork) workMs else restMs
            }
            updateOverlay()
            handler.postDelayed(this, 1000)
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlay = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundResource(com.majid.timer.R.drawable.overlay_bg)
            setPadding(18, 12, 18, 12)
            text = "00:00"
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 120
        }
        makeDraggable(overlay!!, params)
        windowManager.addView(overlay, params)
    }

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX - (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateOverlay() {
        val elapsedMin = elapsedMs / 60000
        val elapsedSec = (elapsedMs / 1000) % 60
        val remMin = remainingMs.coerceAtLeast(0) / 60000
        val remSec = (remainingMs.coerceAtLeast(0) / 1000) % 60
        val mode = if (isWork) "کار" else "استراحت"
        overlay?.text = "$mode | گذشته %02d:%02d | مانده %02d:%02d".format(elapsedMin, elapsedSec, remMin, remSec)
    }

    private fun beep15Seconds() {
        tone?.release()
        tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        val end = SystemClock.elapsedRealtime() + 15000
        val beepRunnable = object : Runnable {
            override fun run() {
                if (SystemClock.elapsedRealtime() < end) {
                    tone?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                    handler.postDelayed(this, 1000)
                } else {
                    tone?.release()
                    tone = null
                }
            }
        }
        handler.post(beepRunnable)
    }

    private fun buildNotification(text: String): Notification {
        val channelId = "timer_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Majid Timer", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, channelId)
            .setContentTitle("Majid Timer فعال است")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        tone?.release()
        overlay?.let { windowManager.removeView(it) }
        overlay = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
