package com.example.puzzleapp.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnTouchPuzzlePiece {
    fun onMovePiece(oldPos: Int, newPos: Int)

    fun onSwipePiece(oldPos: Int, newPos: Int)

    fun onDragViewHolder(viewHolder: RecyclerView.ViewHolder)

    fun onPieceClicked(position: Int, id: Int, view: View)
}