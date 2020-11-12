package com.example.puzzleapp

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController

class Controller(private val callbacks: AdapterCallbacks) :
    TypedEpoxyController<List<Tile>>(
        EpoxyAsyncUtil.getAsyncBackgroundHandler(),
        EpoxyAsyncUtil.getAsyncBackgroundHandler()
    ) {

    var list = arrayListOf<Tile>()

    var lastselected = -1
    override fun buildModels(data: List<Tile>) {
        list = data as ArrayList<Tile>

        data.forEachIndexed { index, tile ->
            tile {
                id(index)
                tile(tile)
                onclick { _ ->
                    callbacks.onClick(index, tile.id)
                }
            }
        }
    }

    private fun compareSelectedTile(position: Int) {
        println("pos $position , right $lastselected")
        println(list[position])
        println(list[lastselected])
        if (position == lastselected) {
            val tileold = list[position]
            val tilenew = list[lastselected]


            list.removeAt(position)
            list.add(position, tilenew)
            list.removeAt(lastselected)
            list.add(lastselected, tileold)
            //list[position] = tilenew
            //list[lastselected] = tileold

            //Toast.makeText(this, "درست بود", Toast.LENGTH_SHORT).show()
            // controllerllll.setData(list)
            //setrecyclerview()
        } else {
            // Toast.makeText(this, "اشتباه بود اسگل", Toast.LENGTH_SHORT).show()

        }
        println(list)
        lastselected = -1
    }

}
