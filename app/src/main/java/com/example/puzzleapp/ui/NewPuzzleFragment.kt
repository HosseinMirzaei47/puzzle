package com.example.puzzleapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.puzzleapp.databinding.FragmentNewPuzzleBinding
import com.example.puzzleapp.models.PuzzleTile
import com.example.puzzleapp.utils.Settings
import com.example.puzzleapp.utils.getBitmapPositionInsideImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@AndroidEntryPoint
class NewPuzzleFragment : Fragment() {

    @Inject
    lateinit var settings: Settings

    private val args: NewPuzzleFragmentArgs by navArgs()

    private lateinit var binding: FragmentNewPuzzleBinding

    private val dificulity by lazy {
        args.difficulty
    }

    private val mode by lazy {
        args.puzzleMode
    }

    private val puzzleTiles = arrayListOf<PuzzleTile>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPuzzleBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAndDisplayPuzzle()
    }

    private fun getAndDisplayPuzzle() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            settings.puzzleType.collect { srcType ->
                if (srcType == Settings.TYPE_DEFAULT) {
                    settings.puzzleSrcDrawable.collect { puzzleSrc ->
                        val imageToSplit =
                            BitmapFactory.decodeResource(resources, puzzleSrc)
                        binding.imageSrc = imageToSplit
                        delay(1000)
                        splitImage(imageToSplit, dificulity)


                    }
                } else if (srcType == Settings.TYPE_CUSTOM) {
                    settings.puzzleSrcPath.collect { puzzleSrc ->
                        val imageToSplit = BitmapFactory.decodeFile(puzzleSrc)
                        binding.imageSrc = imageToSplit
                    }
                }
            }
        }
    }

    private suspend fun splitImage(image: Bitmap, pieceNumbers: Int) {
        val rows: Int
        val pieceHeight: Int
        val pieceWidth: Int

        val dimensions: IntArray = getBitmapPositionInsideImageView(binding.imageView)
        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]
        val croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop)
        val scaledBitmap =
            Bitmap.createScaledBitmap(image, scaledBitmapWidth, scaledBitmapHeight, true)
        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            abs(scaledBitmapLeft),
            abs(scaledBitmapTop),
            croppedImageWidth,
            croppedImageHeight
        )
        val cols = sqrt(pieceNumbers.toDouble()).toInt()
        rows = cols
        pieceHeight = croppedBitmap.height / rows
        pieceWidth = croppedBitmap.width / cols

        var id = 0
        var yCoord = 0
        for (x in 0 until rows) {
            var xCoord = 0
            for (y in 0 until cols) {
                puzzleTiles.add(
                    PuzzleTile(
                        requireContext(),
                        id++,
                        PointF(
                            (xCoord + binding.imageView.left).toFloat(),
                            (yCoord + binding.imageView.top).toFloat()
                        ),
                        Bitmap.createBitmap(
                            croppedBitmap,
                            xCoord,
                            yCoord,
                            pieceWidth,
                            pieceHeight
                        ),
                        pieceWidth.toFloat(),
                        pieceHeight.toFloat()
                    )
                )
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }


        val listofpoints = arrayListOf<PointF>()
        puzzleTiles.forEachIndexed { index, puzzleTiles ->
            listofpoints.add(puzzleTiles.correctPoint)
        }
        listofpoints.shuffle()
        puzzleTiles.forEachIndexed { index, puzzleTiles ->
            puzzleTiles.currentPoint = listofpoints[index]
            puzzleTiles.x = listofpoints[index].x
            puzzleTiles.y = listofpoints[index].y
        }


        withContext(Dispatchers.Main) {
            showPuzzle()
        }

    }

    private fun showPuzzle() {
        puzzleTiles.forEach {
            val params = RelativeLayout.LayoutParams(
                it.width.toInt(),
                it.height.toInt()
            )

            Glide.with(requireActivity()).load(it.bitmap).into(it)
            binding.layout.addView(it, params)

            it.setOnTouchListener(object : View.OnTouchListener {

                private var _xDelta: Float = 0.0f
                private var _yDelta: Float = 0.0f
                private var _xDolta = 0f
                private var _yDolta = 0f
                private var direction = -1

                override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                    when (event!!.action) {

                        MotionEvent.ACTION_DOWN -> {
                            println("mmb down")
                            // println("mmb x= ${event.x} y= ${event.y} raw x= ${event.rawX} y=${event.rawY}")
                            //println("mmb x= ${p0?.x} y= ${p0?.y}")
                            //println("mmb x ${p0!!.x - event.rawX} y ${p0.y - event.rawY}")
                            _xDelta = p0!!.x - event.rawX
                            _yDelta = p0.y - event.rawY
                            _xDolta = event.rawX
                            _yDolta = event.rawY
                            p0.bringToFront()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            println("mmb move")
                            //println("mmb dolta $_xDolta  $_yDolta")
                            // println("mmb ${abs(_xDolta - event.rawX)}  ${abs(_yDolta - event.rawY)}")
                            //  println("mmb ${abs(_xDolta - event.rawX)}  ${abs(_yDolta - event.rawY)}")
                            if (direction < 0) {
                                if (abs(_xDolta - event.rawX) > abs(_yDolta - event.rawY)) {
                                    if ((_xDolta - event.rawX) < 0) {
                                        //println("mmb chap kard")
                                        direction = 3
                                    } else {
                                        //println("mmb rast kard")
                                        direction = 1
                                    }
                                } else {
                                    if ((_yDolta - event.rawY) < 0) {
                                        //println("mmb hava kard")
                                        direction = 4
                                    } else {
                                        // println("mmb zamin kard")
                                        direction = 2
                                    }
                                }
                            }


                            println("mmb ${p0!!.x} ${p0.y}")

                            if (direction == 1 || direction == 3) {
                                if (abs(_xDolta - event.rawX) < p0.width) {
                                    p0!!.animate()
                                        .x(event.rawX + _xDelta)
                                        .setDuration(0)
                                        .start()
                                }

                            } else {
                                if (abs(_yDolta - event.rawY) < p0.height) {
                                    p0!!.animate()
                                        .y(event.rawY + _yDelta)
                                        .setDuration(0)
                                        .start()
                                }
                            }
                        }
                        MotionEvent.ACTION_UP -> {

                            val tile = p0 as PuzzleTile
                            println("mmb ${p0.x} ${p0.y}")
                            p0!!.animate()
                                .x(tile.currentPoint!!.x)
                                .y(tile.currentPoint!!.y)
                                //.setDuration(400)
                                //.rotationBy(360f)
                                .start()

                            //tile.x=tile.currentPoint!!.x
                            //tile.y=tile.currentPoint!!.y
                            println("mmb up")
                            println("mmb ${p0.x} ${p0.y}")
                            println("mmb ${tile.currentPoint!!.x} ${tile.currentPoint!!.y}")
                            when (direction - 20) {
                                1 -> Toast.makeText(requireContext(), "chap", Toast.LENGTH_SHORT)
                                    .show()
                                2 -> Toast.makeText(requireContext(), "hava", Toast.LENGTH_SHORT)
                                    .show()
                                3 -> Toast.makeText(requireContext(), "rast", Toast.LENGTH_SHORT)
                                    .show()
                                4 -> Toast.makeText(requireContext(), "bottom", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            direction = -1
                        }
                        else -> return false
                    }
                    return true
                }
            })
        }
    }
}