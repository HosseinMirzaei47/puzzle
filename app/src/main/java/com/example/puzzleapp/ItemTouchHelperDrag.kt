package com.example.puzzleapp

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperDrag constructor(val onDragAndDrop: OnTouchPuzzleTile) :
    ItemTouchHelper(object : SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT,
        0,
    ) {
        var fromPos = -1
        var toPos = -1
        var viewholdordor: RecyclerView.ViewHolder? = null
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            toPos = target.adapterPosition
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // moveItem(viewHolder.adapterPosition,viewHolder.adapterPosition+1)
            // println("onSwiped")
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    fromPos = viewHolder!!.adapterPosition;
                    viewholdordor = viewHolder
                }
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    //println("action is wsip")
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (fromPos != -1 && toPos != -1
                        && fromPos != toPos
                    ) {
                        //viewholdordor!!.itemView.visibility=View.GONE
                        //   moveItem(fromPos, toPos);
                        onDragAndDrop.onDragViewHolder(viewholdordor!!)
                        onDragAndDrop.onMoveTile(fromPos, toPos)

                        //viewholdordor!!.itemView.visibility=View.VISIBLE
                        fromPos = -1;
                        toPos = -1;
                    }
                }
            }
        }
    })