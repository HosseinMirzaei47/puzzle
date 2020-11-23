package com.example.puzzleapp.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.example.puzzleapp.models.JigsawPiece

class TouchListener(
    private val onJigsawPiece: OnJigsawPiece
) : OnTouchListener {
    private var xDelta = 0f
    private var yDelta = 0f
    private var previousX: Float = 0.0f
    private var previousY: Float = 0.0f
    private var firstRawX = 0f
    private var firstRawY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.rawX
        val y = motionEvent.rawY
        val piece: JigsawPiece = view as JigsawPiece

        if (!piece.canMove) {
            return true
        }
        val lParams = view.layoutParams as RelativeLayout.LayoutParams
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                previousX = view.x - motionEvent.rawX
                previousY = view.y - motionEvent.rawY
                firstRawX = motionEvent.rawX
                firstRawY = motionEvent.rawY

                xDelta = x - lParams.leftMargin
                yDelta = y - lParams.topMargin
                piece.bringToFront()
            }
            MotionEvent.ACTION_MOVE -> {
                view.animate()
                    .x(motionEvent.rawX + previousX)
                    .y(motionEvent.rawY + previousY)
                    .setDuration(0)
                    .start()
            }
            MotionEvent.ACTION_UP -> {
                val xDiff: Int = kotlin.math.abs(piece.xCoord - piece.x.toInt())
                val yDiff: Int = kotlin.math.abs(piece.yCoord - piece.y.toInt())
                if (xDiff <= 150 && yDiff <= 150) {
                    view.animate()
                        .x(piece.xCoord.toFloat())
                        .y(piece.yCoord.toFloat())
                        .setDuration(0)
                        .start()
                    piece.canMove = false
                    sendViewToBack(piece)
                    onJigsawPiece.onJigsawPiece(piece)
                }
            }
        }
        return true
    }

    private fun sendViewToBack(child: View) {
        val parent = child.parent as ViewGroup
        parent.removeView(child)
        parent.addView(child, 0)
    }

}