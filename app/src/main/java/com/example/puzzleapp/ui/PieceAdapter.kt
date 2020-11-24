package com.example.puzzleapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.puzzleapp.databinding.PieceItemRowBinding
import com.example.puzzleapp.models.ClickPuzzlePiece

class PieceAdapter constructor(
    private val onTouchPiece: (position: Int, view: View) -> Unit,
    private val itemsAreReplaceable: Boolean
) : RecyclerView.Adapter<PuzzlePieceViewHolder>() {

    var pieces = mutableListOf<ClickPuzzlePiece>()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzlePieceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = PieceItemRowBinding.inflate(
            layoutInflater,
            parent,
            false
        )
        return PuzzlePieceViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PuzzlePieceViewHolder, position: Int) {
        val puzzlePiece = pieces[position]
        if (itemsAreReplaceable) {
            holder.itemView.setOnClickListener {
                onTouchPiece(position, holder.itemView)
            }
        }
        holder.bind(puzzlePiece)
    }

    override fun getItemCount(): Int {
        return pieces.size
    }
}
