package com.example.puzzleapp.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnTouchPuzzleTile {
    fun onMoveTile(oldPos: Int, newPos: Int)

    fun onSwipeTile(oldPos: Int, newPos: Int)

    fun onDragViewHolder(viewHolder: RecyclerView.ViewHolder)

    fun onPieceClicked(position: Int, id: Int, view: View)
}