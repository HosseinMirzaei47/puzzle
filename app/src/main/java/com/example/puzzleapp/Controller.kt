package com.example.puzzleapp

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController

class Controller(private val callbacks: AdapterCallbacks) :
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
                onPieceClicked { _ ->
                    callbacks.onPieceClicked(index, tile.id)
                }
            }
        }
    }

}
