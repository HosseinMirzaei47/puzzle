package com.example.puzzleapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.EpoxyTouchHelper.DragCallbacks
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), AdapterCallbacks {

    private val list = arrayListOf<Tile>()
    private val controller = Controller(this)

    var lastselected: Int = -1
    var lastselectedindex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val origialBitmap = BitmapFactory.decodeResource(resources, R.drawable.puzzle_image)

        splitImage(origialBitmap, 9)

        list.shuffle()
        recyclerview.layoutManager = GridLayoutManager(this, 3)
        recyclerview.setController(controller)
        controller.setData(list)
    }

    private fun compareSelectedTile(position: Int, id: Int) {
        val tileold = list[position]
        val tilenew = list[lastselectedindex]

        list[position] = tilenew
        list[lastselectedindex] = tileold

        Toast.makeText(this, "درست بود", Toast.LENGTH_SHORT).show()
        controller.setData(list)
        Toast.makeText(this, "اشتباه بود اسگل", Toast.LENGTH_SHORT).show()

        println(list)
        lastselected = -1
    }

    override fun onClick(index: Int, id: Int) {
        if (lastselected < 0) {
            lastselected = id
            lastselectedindex = index
        } else {
            compareSelectedTile(index, id)
        }
    }

    private fun splitImage(image: Bitmap, chunkNumbers: Int) {

        val rows: Int

        val chunkHeight: Int
        val chunkWidth: Int

        val chunkedImages = ArrayList<Bitmap>(chunkNumbers)

        val scaledBitmap = Bitmap.createScaledBitmap(image, image.width, image.height, true)
        val cols = sqrt(chunkNumbers.toDouble()).toInt()
        rows = cols
        chunkHeight = image.height / rows
        chunkWidth = image.width / cols

        var indexof: Int = 0
        var yCoord = 0
        for (x in 0 until rows) {
            var xCoord = 0
            for (y in 0 until cols) {
                list.add(
                    Tile(
                        indexof++,
                        Bitmap.createBitmap(
                            scaledBitmap,
                            xCoord,
                            yCoord,
                            chunkWidth,
                            chunkHeight
                        )
                    )
                )
                xCoord += chunkWidth
            }
            yCoord += chunkHeight
        }

    }

    private fun setUpTouch() {

        EpoxyTouchHelper.initDragging(controller)
            .withRecyclerView(recyclerview)
            .forGrid()
            .withTarget(TileBindingModel_::class.java)
            .andCallbacks(object : DragCallbacks<TileBindingModel_>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: TileBindingModel_?,
                    itemView: View?
                ) {
                    val carouselIndex: Int = list.indexOf(modelBeingMoved!!.tile())
                    list
                        .add(
                            carouselIndex + (toPosition - fromPosition),
                            list.removeAt(carouselIndex)
                        )

                }


                override fun clearView(model: TileBindingModel_?, itemView: View?) {
                    onDragReleased(model, itemView)
                }

            })

    }

    fun setUpRecycler() {
        recyclerview.withModels {
            list.forEachIndexed { index, tile ->
                tile {
                    id(index)
                    tile(tile)
                    onclick { _ ->
                        Log.d("DSF", tile.toString())
                        if (lastselected < 0) {
                            lastselected = tile.id
                            println("lastseleceted updated $lastselected")
                        } else {
                            println("go to function by $index and $lastselected")
                            compareSelectedTile(index, 0)
                        }
                    }
                }
            }
        }
    }


}