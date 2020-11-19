package com.example.puzzleapp.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import androidx.appcompat.widget.AppCompatImageView

class PuzzleTile(
    context: Context,
    val idindex: Int,
    val correctPoint: PointF,
    val bitmap: Bitmap,
    val width:Float,
    val height:Float
) : AppCompatImageView(context) {
    var currentPoint: PointF? = null
}