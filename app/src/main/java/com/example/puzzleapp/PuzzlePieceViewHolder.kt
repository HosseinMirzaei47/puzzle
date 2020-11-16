package com.example.puzzleapp

import androidx.recyclerview.widget.RecyclerView
import com.example.puzzleapp.databinding.PieceItemRowBinding
import com.example.puzzleapp.models.PuzzlePiece

class PuzzlePieceViewHolder(val binding: PieceItemRowBinding) :
    RecyclerView.ViewHolder(
        binding.root
    ) {

    fun bind(tile: PuzzlePiece) {
        binding.tile = tile
        // binding.onPieceClicked=onClickListener
    }
}