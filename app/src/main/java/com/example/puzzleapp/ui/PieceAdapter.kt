package com.example.puzzleapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.puzzleapp.databinding.PieceItemRowBinding
import com.example.puzzleapp.models.PuzzlePiece
import com.example.puzzleapp.utils.OnTouchPuzzleTile

class PieceAdapter constructor(
    private val onTouchPuzzleTile: OnTouchPuzzleTile,
    private val itemsAreReplaceable: Boolean
) : RecyclerView.Adapter<PuzzlePieceViewHolder>() {

    var pieces = mutableListOf<PuzzlePiece>()
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
                onTouchPuzzleTile.onPieceClicked(position, puzzlePiece.id, holder.itemView)
            }
        }
        holder.bind(puzzlePiece)
    }

    override fun getItemCount(): Int {
        return pieces.size
    }

    /*ListAdapter<PuzzlePiece, RecyclerviewAdapter.ViewHolder>(UserItemDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val layoutInflater = LayoutInflater.from(parent.context)
      val itemBinding: RecyclerviewTileBinding = RecyclerviewTileBinding.inflate(
          layoutInflater,
          parent,
          false
      )
      return ViewHolder(itemBinding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(getItem(position))
  }

  override fun getItemCount(): Int {
      val count = super.getItemCount()
      return when (count) {
          0 -> 1
          else -> count
      }
  }
*/

}
