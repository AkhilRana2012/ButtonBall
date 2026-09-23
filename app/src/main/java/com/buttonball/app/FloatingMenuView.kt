package com.buttonball.app

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView

class FloatingMenuView(context: Context, private val actions: Actions, ballX: Int, ballY: Int) : FrameLayout(context) {
    interface Actions { fun lock(); fun volume(); fun power(); fun dismiss() }

    init {
        val size = dp(52)
        val centerX = ballX + size / 2
        val centerY = ballY + size / 2
        // Fan inward from the corner; clamping also makes this usable after moving to another edge.
        addAction("⏻", "Power", centerX - dp(112), centerY - dp(42), size) { actions.power() }
        addAction("▣", "Sleep / lock", centerX - dp(78), centerY - dp(102), size) { actions.lock() }
        addAction("🔊", "Volume", centerX - dp(38), centerY - dp(162), size) { actions.volume() }
        setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) actions.dismiss()
            true
        }
    }

    private fun addAction(icon: String, description: String, rawX: Int, rawY: Int, size: Int, click: () -> Unit) {
        val button = TextView(context).apply {
            gravity = android.view.Gravity.CENTER
            text = icon
            textSize = if (icon == "🔊") 22f else 29f
            contentDescription = description
            background = context.getDrawable(R.drawable.menu_action_background)
            elevation = dp(6).toFloat()
            setOnClickListener { click() }
        }
        val x = rawX.coerceIn(dp(4), resources.displayMetrics.widthPixels - size - dp(4))
        val y = rawY.coerceIn(dp(4), resources.displayMetrics.heightPixels - size - dp(4))
        addView(button, LayoutParams(size, size).apply { leftMargin = x; topMargin = y })
        button.translationX = -dp(32).toFloat()
        button.translationY = dp(32).toFloat()
        button.alpha = 0f
        button.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(190)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    fun retract(onComplete: () -> Unit) {
        isEnabled = false
        var remaining = childCount
        if (remaining == 0) { onComplete(); return }
        repeat(childCount) { index ->
            getChildAt(index).animate().translationX(-dp(32).toFloat()).translationY(dp(32).toFloat())
                .alpha(0f).setDuration(150).setInterpolator(DecelerateInterpolator()).withEndAction {
                    remaining--
                    if (remaining == 0) onComplete()
                }.start()
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()
}
