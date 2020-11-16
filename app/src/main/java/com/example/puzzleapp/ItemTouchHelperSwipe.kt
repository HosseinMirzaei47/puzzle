package com.example.puzzleapp

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperSwipe(val onSwipeItemListener: OnTouchPuzzleTile, directionsof: Int) :
    ItemTouchHelper(object : SimpleCallback(
        directionsof,
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
            //onSwipeItemListener.onSwiping(fromPos,toPos)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwipeItemListener.onSwipeTile(viewHolder.adapterPosition, direction)
            // moveItem(viewHolder.adapterPosition,viewHolder.adapterPosition+1)
            // println("onSwiped")
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            println("onSelectedChanged ${viewHolder?.adapterPosition} and $actionState")
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    fromPos = viewHolder!!.adapterPosition;
                }
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    //println("action is wsip")
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (fromPos != -1 && toPos != -1
                        && fromPos != toPos
                    ) {
                        onSwipeItemListener.onSwipeTile(fromPos, toPos)
                        //   moveItem(fromPos, toPos);
                        //onDragAndDrop.onMoving(fromPos, toPos)
                        fromPos = -1;
                        toPos = -1;
                    }
                }
            }
        }
    })
