package com.example.puzzleapp

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController

class Controller(private val callbacks: AdapterCallbacks) :
    TypedEpoxyController<List<Tile>>(
        EpoxyAsyncUtil.getAsyncBackgroundHandler(),
        EpoxyAsyncUtil.getAsyncBackgroundHandler()
    ) {

    var puzzlePieces = arrayListOf<Tile>()

    override fun buildModels(data: List<Tile>) {
        puzzlePieces = data as ArrayList<Tile>

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
