package com.example.puzzleapp.ui

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import com.example.puzzleapp.models.PuzzlePiece
import com.example.puzzleapp.tile

class Controller :
    TypedEpoxyController<List<PuzzlePiece>>(
        EpoxyAsyncUtil.getAsyncBackgroundHandler(),
        EpoxyAsyncUtil.getAsyncBackgroundHandler()
    ) {

    var puzzlePieces = arrayListOf<PuzzlePiece>()

    override fun buildModels(data: List<PuzzlePiece>) {
        puzzlePieces = data as ArrayList<PuzzlePiece>

        data.forEachIndexed { index, tile ->
            tile {
                id(index)
                tile(tile)
                onPieceClicked { view ->
                    /*callbacks.onPieceClicked(index, tile.id, view)*/
                }
            }
        }
    }
}
