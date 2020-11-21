package com.example.puzzleapp.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
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

    private val difficulty by lazy {
        args.difficulty
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
                        splitImage(imageToSplit, difficulty)


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

        var id = -1
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
                        pieceHeight.toFloat(),
                        canMove(id, 1, pieceNumbers),
                        canMove(id, 2, pieceNumbers),
                        canMove(id, 3, pieceNumbers),
                        canMove(id, 4, pieceNumbers),
                    )
                )
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }

        val listOfPoints = arrayListOf<PointF>()
        puzzleTiles.forEachIndexed { _, puzzleTiles ->
            listOfPoints.add(puzzleTiles.correctPoint)
        }

        listOfPoints.shuffle()

        puzzleTiles.forEachIndexed { index, puzzleTiles ->
            puzzleTiles.currentPoint = listOfPoints[index]
            puzzleTiles.x = listOfPoints[index].x
            puzzleTiles.y = listOfPoints[index].y
        }

        withContext(Dispatchers.Main) {
            showPuzzle()
        }

    }

    private fun canMove(index: Int, direction: Int, pieceNumbers: Int): Boolean {
        val rows = sqrt(pieceNumbers.toDouble()).toInt()
        if (direction == 1) {
            if (index % rows == 0) {
                return false
            }

        } else if (direction == 2) {
            if (index > rows) {
                return false
            }

        } else if (direction == 3) {
            if ((index + 1) % rows == 0) {
                return false
            }

        } else if (direction == 4) {
            if (index > (pieceNumbers - rows)) {
                return false
            }
        }
        return true
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

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when (event!!.action) {

                        MotionEvent.ACTION_DOWN -> {
                            _xDelta = view!!.x - event.rawX
                            _yDelta = view.y - event.rawY
                            _xDolta = event.rawX
                            _yDolta = event.rawY
                            view.bringToFront()
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = abs(_xDolta - event.rawX)
                            val deltaY = abs(_yDolta - event.rawY)

                            if (direction < 0) {
                                direction = if (deltaX <= deltaY) {
                                    if ((_yDolta - event.rawY) < 0) {
                                        4
                                    } else {
                                        2
                                    }
                                } else {
                                    if ((_xDolta - event.rawX) < 0) {
                                        3
                                    } else {
                                        1
                                    }
                                }
                            }

                            val tile = view as PuzzleTile

                            when (direction) {
                                1 -> {
                                    if (deltaX < tile.width && tile.canMoveLeft) {
                                        view.animate()
                                            .x(event.rawX + _xDelta)
                                            .start()
                                    }
                                }

                                3 -> {
                                    if (deltaX > tile.width && tile.canMoveRight) {
                                        if (deltaX < tile.width && tile.canMoveLeft) {
                                            view.animate()
                                                .x(event.rawX + _xDelta)
                                                .start()
                                        }
                                    }
                                }

                                2 -> {
                                    if (deltaY < tile.height && tile.canMoveTop) {
                                        view.animate()
                                            .y(event.rawY + _yDelta)
                                            .start()
                                    }
                                }
                                else -> {
                                    if (deltaY < tile.height && tile.canMoveBottom) {
                                        view.animate()
                                            .y(event.rawY + _yDelta)
                                            .start()
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {

                            val tile = view as PuzzleTile

                            view.animate()
                                .x(tile.currentPoint!!.x)
                                .y(tile.currentPoint!!.y)
                                .start()

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