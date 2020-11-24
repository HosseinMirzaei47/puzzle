package com.example.puzzleapp.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import com.example.puzzleapp.models.PuzzlePiece

class JigsawPieceTouchListener(
    private val onJigsawPieceTouch: (puzzlePiece: PuzzlePiece) -> Unit
) : OnTouchListener {
    private var previousX: Float = 0.0f
    private var previousY: Float = 0.0f
    private var firstRawX = 0f
    private var firstRawY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val piece: PuzzlePiece = view as PuzzlePiece

        if (!piece.canMove) {
            return true
        }
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                previousX = view.x - motionEvent.rawX
                previousY = view.y - motionEvent.rawY
                firstRawX = motionEvent.rawX
                firstRawY = motionEvent.rawY

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
                val xDiff: Int = kotlin.math.abs(piece.correctPoint.x - piece.x.toInt()).toInt()
                val yDiff: Int = kotlin.math.abs(piece.correctPoint.y - piece.y.toInt()).toInt()
                if (xDiff <= 150 && yDiff <= 150) {
                    view.animate()
                        .x(piece.correctPoint.x)
                        .y(piece.correctPoint.y)
                        .setDuration(0)
                        .start()
                    piece.canMove = false
                    sendViewToBack(piece)
                    onJigsawPieceTouch(piece)
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