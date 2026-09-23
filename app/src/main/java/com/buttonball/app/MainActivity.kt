package com.buttonball.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.app.Activity

class MainActivity : Activity() {
    private lateinit var overlayStatus: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var runningStatus: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overlayStatus = findViewById(R.id.overlayStatus)
        accessibilityStatus = findViewById(R.id.accessibilityStatus)
        runningStatus = findViewById(R.id.runningStatus)
        startButton = findViewById(R.id.startButton)

        findViewById<Button>(R.id.overlayButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        startButton.setOnClickListener {
            val service = ButtonBallAccessibilityService.instance
            if (!Settings.canDrawOverlays(this) || !isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Enable both permissions first.", Toast.LENGTH_SHORT).show()
            } else {
                // The accessibility service owns the overlay as soon as it connects.
                service?.showBall()
                Toast.makeText(this, "ButtonBall is ready. You can leave setup now.", Toast.LENGTH_SHORT).show()
                finish()
            }
            updateStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val overlayEnabled = Settings.canDrawOverlays(this)
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val running = ButtonBallAccessibilityService.instance?.isBallVisible() == true
        overlayStatus.text = getString(R.string.status_overlay, getString(if (overlayEnabled) R.string.on else R.string.off))
        accessibilityStatus.text = getString(R.string.status_accessibility, getString(if (accessibilityEnabled) R.string.on else R.string.off))
        runningStatus.text = getString(R.string.status_running, getString(if (running) R.string.running else R.string.stopped))
        startButton.isEnabled = overlayEnabled && accessibilityEnabled
        startButton.text = getString(R.string.start)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, ButtonBallAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }
}
