package com.example.puzzleapp.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperDrag constructor(val onDragAndDrop: OnTouchPuzzleTile) :
    ItemTouchHelper(object : SimpleCallback(
        UP or DOWN or RIGHT or LEFT,
        0,
    ) {
        var fromPos = -1
        var toPos = -1
        var viewholder: RecyclerView.ViewHolder? = null
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            toPos = target.adapterPosition
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            when (actionState) {
                ACTION_STATE_DRAG -> {
                    fromPos = viewHolder!!.adapterPosition
                    viewholder = viewHolder
                }
                ACTION_STATE_IDLE -> {
                    if (fromPos != -1 && toPos != -1
                        && fromPos != toPos
                    ) {
                        onDragAndDrop.onDragViewHolder(viewholder!!)
                        onDragAndDrop.onMoveTile(fromPos, toPos)
                        fromPos = -1
                        toPos = -1
                    }
                }
            }
        }
    })