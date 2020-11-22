package com.example.puzzleapp.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperSwipe(val onSwipeItemListener: OnTouchPuzzlePiece, directions: Int) :
    ItemTouchHelper(object : SimpleCallback(
        directions,
        0
    ) {
        var fromPos = -1
        var toPos = -1
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            println("onMove ${target.adapterPosition}")
            if (toPos == -1) {
                toPos = target.adapterPosition
            }
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwipeItemListener.onSwipePiece(viewHolder.adapterPosition, direction)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            when (actionState) {
                ACTION_STATE_DRAG -> {
                    fromPos = viewHolder!!.adapterPosition
                }
                ACTION_STATE_IDLE -> {
                    if (fromPos != -1 && toPos != -1
                        && fromPos != toPos
                    ) {
                        onSwipeItemListener.onSwipePiece(fromPos, toPos)
                        fromPos = -1
                        toPos = -1
                    }
                }
            }
        }
    })
