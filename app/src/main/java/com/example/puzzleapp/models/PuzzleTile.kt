package com.example.puzzleapp.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import androidx.appcompat.widget.AppCompatImageView

class PuzzleTile(
    context: Context,
    val idIndex: Int,
    val correctPoint: PointF,
    val bitmap: Bitmap,
    val width: Float,
    val height: Float
) : AppCompatImageView(context) {
    var currentPoint: PointF? = null
    var canMoveLeft: Boolean = false
    var canMoveTop: Boolean = false
    var canMoveRight: Boolean = false
    var canMoveBottom: Boolean = false
}