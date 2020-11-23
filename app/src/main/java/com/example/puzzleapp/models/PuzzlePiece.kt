package com.example.puzzleapp.models

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import androidx.appcompat.widget.AppCompatImageView

@SuppressLint("ViewConstructor")
class PuzzlePiece(
    context: Context,
    val width: Float,
    val height: Float
) : AppCompatImageView(context) {
    lateinit var correctPoint: PointF
    var currentPoint: PointF? = null
    var canMoveLeft: Boolean = false
    var canMoveTop: Boolean = false
    var canMoveRight: Boolean = false
    var canMoveBottom: Boolean = false
    var canMove = true
    var bitmap: Bitmap? = null
    var position = -1
    var correctPosition = -1
}