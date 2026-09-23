package com.buttonball.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs

class ButtonBallAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private lateinit var preferences: SharedPreferences
    private var ball: FloatingBallView? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private var menu: FloatingMenuView? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        showBall()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        removeOverlays(immediate = true)
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        removeOverlays(immediate = true)
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun isBallVisible() = ball != null

    fun showBall() {
        if (ball != null || !android.provider.Settings.canDrawOverlays(this)) return
        val size = dp(BALL_SIZE_DP)
        val x = preferences.getInt(KEY_X, resources.displayMetrics.widthPixels - size / 2)
        val y = preferences.getInt(KEY_Y, resources.displayMetrics.heightPixels - size / 2)
        val params = ballLayoutParams(x, y)
        val view = FloatingBallView(this, object : FloatingBallView.Callbacks {
            override fun onDragStarted() = closeMenu()
            override fun onDragTo(x: Int, y: Int) = moveBall(x, y, save = false)
            override fun onDragFinished(x: Int, y: Int) = moveBall(x, y, save = true)
            override fun onTapped() { if (menu == null) showMenu() else closeMenu() }
            override fun onOutwardSwipe() { if (menu == null) showMenu() }
        })
        view.callbacksPositionX = x
        view.callbacksPositionY = y
        try {
            windowManager.addView(view, params)
            ball = view
            ballParams = params
        } catch (_: WindowManager.BadTokenException) {
            // Permission may have been withdrawn while the service was connected.
        }
    }

    fun hideBall() = removeOverlays()

    private fun moveBall(requestedX: Int, requestedY: Int, save: Boolean) {
        val view = ball ?: return
        val params = ballParams ?: return
        val size = dp(BALL_SIZE_DP)
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val half = size / 2
        if (save) {
            // Snap to the closest edge, keeping half the handle beyond that edge.
            val candidates = listOf(
                -half to requestedY.coerceIn(-half, height - half),
                width - half to requestedY.coerceIn(-half, height - half),
                requestedX.coerceIn(-half, width - half) to -half,
                requestedX.coerceIn(-half, width - half) to height - half
            )
            val best = candidates.minByOrNull { abs(requestedX - it.first) + abs(requestedY - it.second) }!!
            params.x = best.first
            params.y = best.second
        } else {
            params.x = requestedX.coerceIn(-half, width - half)
            params.y = requestedY.coerceIn(-half, height - half)
        }
        view.callbacksPositionX = params.x
        view.callbacksPositionY = params.y
        windowManager.updateViewLayout(view, params)
        if (save) preferences.edit().putInt(KEY_X, params.x).putInt(KEY_Y, params.y).apply()
    }

    private fun showMenu() {
        val ballParameters = ballParams ?: return
        if (menu != null) return
        val view = FloatingMenuView(this, object : FloatingMenuView.Actions {
            override fun lock() { performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN); closeMenu() }
            override fun volume() {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                closeMenu()
            }
            override fun power() { performGlobalAction(GLOBAL_ACTION_POWER_DIALOG); closeMenu() }
            override fun dismiss() = closeMenu()
        }, ballParameters.x, ballParameters.y)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        try { windowManager.addView(view, params); menu = view } catch (_: WindowManager.BadTokenException) { }
    }

    private fun closeMenu(immediate: Boolean = false) {
        val current = menu ?: return
        if (immediate) {
            safelyRemove(current)
            menu = null
        } else {
            current.retract {
                safelyRemove(current)
                if (menu === current) menu = null
            }
        }
    }

    private fun removeOverlays(immediate: Boolean = false) {
        closeMenu(immediate)
        ball?.let { safelyRemove(it) }
        ball = null
        ballParams = null
    }

    private fun safelyRemove(view: android.view.View) {
        try { windowManager.removeView(view) } catch (_: IllegalArgumentException) { }
    }

    private fun ballLayoutParams(x: Int, y: Int) = WindowManager.LayoutParams(
        dp(BALL_SIZE_DP), dp(BALL_SIZE_DP), overlayType(),
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }

    private fun overlayType() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private const val PREFERENCES = "buttonball"
        private const val KEY_X = "ball_x"
        private const val KEY_Y = "ball_y"
        private const val BALL_SIZE_DP = 52
        @Volatile var instance: ButtonBallAccessibilityService? = null
            private set
    }
}
