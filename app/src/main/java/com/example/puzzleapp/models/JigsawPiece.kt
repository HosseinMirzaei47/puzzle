package com.example.puzzleapp.models

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.widget.AppCompatImageView

class JigsawPiece(context: Context) : AppCompatImageView(context) {
    var xCoord = 0
    var yCoord = 0
    var pieceWidth = 0
    var pieceHeight = 0
    var canMove = true
    var bitmap:Bitmap?=null
}