package com.buttonball.app

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView
import kotlin.math.abs

class FloatingBallView(context: Context, private val callbacks: Callbacks) : TextView(context) {
    interface Callbacks {
        fun onDragStarted()
        fun onDragTo(x: Int, y: Int)
        fun onDragFinished(x: Int, y: Int)
        fun onTapped()
        fun onOutwardSwipe()
    }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0
    private var startY = 0
    private var dragging = false
    private var openingGesture = false

    init {
        val size = dp(54)
        layoutParams = android.view.ViewGroup.LayoutParams(size, size)
        gravity = Gravity.CENTER
        text = ""
        setTextColor(Color.WHITE)
        contentDescription = "ButtonBall"
        background = context.getDrawable(R.drawable.ball_background)
        elevation = dp(8).toFloat()
        setOnTouchListener { _, event -> handleTouch(event) }
    }

    private fun handleTouch(event: MotionEvent): Boolean = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            downRawX = event.rawX; downRawY = event.rawY
            startX = callbacksPositionX; startY = callbacksPositionY
            dragging = false
            openingGesture = false
            true
        }
        MotionEvent.ACTION_MOVE -> {
            val dx = (event.rawX - downRawX).toInt()
            val dy = (event.rawY - downRawY).toInt()
            if (!dragging && !openingGesture && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                // A left/up motion is the natural way to open a handle parked on the right/bottom.
                if (dx < -dp(32) || (dx < -touchSlop && dy < -dp(24))) {
                    openingGesture = true
                    callbacks.onOutwardSwipe()
                } else {
                    dragging = true
                    callbacks.onDragStarted()
                }
            }
            if (dragging) callbacks.onDragTo(startX + dx, startY + dy)
            true
        }
        MotionEvent.ACTION_UP -> {
            if (dragging) callbacks.onDragFinished(startX + (event.rawX - downRawX).toInt(), startY + (event.rawY - downRawY).toInt()) else if (!openingGesture) callbacks.onTapped()
            true
        }
        MotionEvent.ACTION_CANCEL -> { if (dragging) callbacks.onDragFinished(startX, startY); true }
        else -> true
    }

    var callbacksPositionX: Int = 0
    var callbacksPositionY: Int = 0
    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
}
