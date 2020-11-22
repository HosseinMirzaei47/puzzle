package com.example.puzzleapp.ui

import androidx.recyclerview.widget.RecyclerView
import com.example.puzzleapp.databinding.PieceItemRowBinding
import com.example.puzzleapp.models.ClickPuzzlePiece

class PuzzlePieceViewHolder(val binding: PieceItemRowBinding) :
    RecyclerView.ViewHolder(
        binding.root
    ) {

    fun bind(piece: ClickPuzzlePiece) {
        binding.piece = piece
        // binding.onPieceClicked=onClickListener
    }
}