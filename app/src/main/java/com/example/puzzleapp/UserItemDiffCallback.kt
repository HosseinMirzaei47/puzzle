package com.example.puzzleapp

import androidx.recyclerview.widget.DiffUtil
import com.example.puzzleapp.models.PuzzlePiece

class UserItemDiffCallback : DiffUtil.ItemCallback<PuzzlePiece>() {
    override fun areItemsTheSame(oldItem: PuzzlePiece, newItem: PuzzlePiece): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: PuzzlePiece, newItem: PuzzlePiece): Boolean =
        oldItem.id == newItem.id

}