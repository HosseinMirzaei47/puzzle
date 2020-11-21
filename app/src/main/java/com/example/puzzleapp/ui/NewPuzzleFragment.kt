package com.example.puzzleapp.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.puzzleapp.databinding.FragmentNewPuzzleBinding
import com.example.puzzleapp.models.PuzzleTile
import com.example.puzzleapp.utils.Settings
import com.example.puzzleapp.utils.getBitmapPositionInsideImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@AndroidEntryPoint
class NewPuzzleFragment : Fragment() {

    private lateinit var binding: FragmentNewPuzzleBinding
    private val args: NewPuzzleFragmentArgs by navArgs()

    @Inject
    lateinit var settings: Settings
    private lateinit var navigationDelay: CountDownTimer
    private lateinit var previewTimer: CountDownTimer

    private val pieceNumbers by lazy {
        args.difficulty
    }
    private val correctItemsIds = mutableSetOf<Int>()
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
                        createPreviewCountDown(imageToSplit)
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

    private suspend fun createPreviewCountDown(imageToSplit: Bitmap) {
        withContext(Dispatchers.Main) { binding.progressAnimation.visibility = View.GONE }
        viewLifecycleOwner.lifecycleScope.launch {
            previewTimer = object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val previewTimer = "Preview timer: ${(millisUntilFinished / 1000)}"
                    binding.tvCountDown.text = previewTimer
                }

                override fun onFinish() {
                    viewLifecycleOwner.lifecycleScope.launch {
                        binding.isPlaying = true
                        binding.previewVisibility = View.GONE
                        splitImage(imageToSplit, pieceNumbers)
                    }
                }
            }
            previewTimer.start()
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

        val listOfPoints = arrayListOf<PointF>()
        puzzleTiles.forEachIndexed { _, puzzleTiles ->
            listOfPoints.add(puzzleTiles.correctPoint)
        }
        val previousFist = puzzleTiles[0]
        while (puzzleTiles[0] == previousFist) {
            puzzleTiles.shuffle()
        }
        puzzleTiles.forEachIndexed { index, puzzleTiles ->
            puzzleTiles.currentPoint = listOfPoints[index]
            puzzleTiles.x = listOfPoints[index].x
            puzzleTiles.y = listOfPoints[index].y
            puzzleTiles.canMoveLeft = canMove(index, DIRECTION_LEFT, pieceNumbers)
            puzzleTiles.canMoveTop = canMove(index, DIRECTION_TOP, pieceNumbers)
            puzzleTiles.canMoveRight = canMove(index, DIRECTION_RIGHT, pieceNumbers)
            puzzleTiles.canMoveBottom = canMove(index, DIRECTION_BOTTOM, pieceNumbers)
            puzzleTiles.position = index
        }
        (0 until puzzleTiles.size).forEach { index ->
            val tile = puzzleTiles[index]
            if (tile.position == tile.correctPosition) {
                correctItemsIds.add(tile.correctPosition)
            }
        }

        withContext(Dispatchers.Main) {
            showPuzzle()
        }
    }

    private fun canMove(index: Int, direction: Int, pieceNumbers: Int): Boolean {
        val rows = sqrt(pieceNumbers.toDouble()).toInt()
        if (direction == DIRECTION_LEFT) {
            if (index % rows == 0) return false

        } else if (direction == DIRECTION_TOP) {
            if (index < rows) return false

        } else if (direction == DIRECTION_RIGHT) {
            if ((index + 1) % rows == 0) return false

        } else if (direction == DIRECTION_BOTTOM) {
            if (index >= (pieceNumbers - rows)) return false
        }
        return true
    }

    fun performMovementAction(draggedTile: PuzzleTile, direction: Int) {
        val tileToBeReplaced: PuzzleTile?

        when (direction) {
            DIRECTION_LEFT -> {
                if (!draggedTile.canMoveLeft) {
                    return
                }
                tileToBeReplaced = puzzleTiles[draggedTile.position - 1]
                replacePieces(draggedTile, tileToBeReplaced)
            }

            DIRECTION_RIGHT -> {
                if (!draggedTile.canMoveRight) {
                    return
                }
                tileToBeReplaced = puzzleTiles[draggedTile.position + 1]
                replacePieces(draggedTile, tileToBeReplaced)
            }

            DIRECTION_TOP -> {
                if (!draggedTile.canMoveTop) {
                    return
                }
                tileToBeReplaced =
                    puzzleTiles[draggedTile.position - sqrt(pieceNumbers.toDouble()).toInt()]
                replacePieces(draggedTile, tileToBeReplaced)
            }

            else -> {
                if (!draggedTile.canMoveBottom) {
                    return
                }
                tileToBeReplaced =
                    puzzleTiles[draggedTile.position + sqrt(pieceNumbers.toDouble()).toInt()]
                replacePieces(draggedTile, tileToBeReplaced)
            }
        }

        tileToBeReplaced.animate()
            .x(tileToBeReplaced.currentPoint!!.x)
            .y(tileToBeReplaced.currentPoint!!.y)
            .setDuration(400)
            .rotationBy(360f)
            .start()

        draggedTile.animate()
            .x(draggedTile.currentPoint!!.x)
            .y(draggedTile.currentPoint!!.y)
            .setDuration(400)
            .start()
    }

    private fun replacePieces(
        draggedTile: PuzzleTile,
        tileToBeReplaced: PuzzleTile
    ) {
        puzzleTiles[tileToBeReplaced.position] = draggedTile
        puzzleTiles[draggedTile.position] = tileToBeReplaced

        val tempPositionHolder = draggedTile.position
        draggedTile.position = tileToBeReplaced.position
        tileToBeReplaced.position = tempPositionHolder

        val tempPointHolder = draggedTile.currentPoint
        draggedTile.currentPoint = tileToBeReplaced.currentPoint
        tileToBeReplaced.currentPoint = tempPointHolder

        val tempCanMoveLeftHolder = draggedTile.canMoveLeft
        draggedTile.canMoveLeft = tileToBeReplaced.canMoveLeft
        tileToBeReplaced.canMoveLeft = tempCanMoveLeftHolder

        val tempCanMoveTopHolder = draggedTile.canMoveTop
        draggedTile.canMoveTop = tileToBeReplaced.canMoveTop
        tileToBeReplaced.canMoveTop = tempCanMoveTopHolder

        val tempCanMoveRightHolder = draggedTile.canMoveRight
        draggedTile.canMoveRight = tileToBeReplaced.canMoveRight
        tileToBeReplaced.canMoveRight = tempCanMoveRightHolder

        val tempCanMoveBottomHolder = draggedTile.canMoveBottom
        draggedTile.canMoveBottom = tileToBeReplaced.canMoveBottom
        tileToBeReplaced.canMoveBottom = tempCanMoveBottomHolder

        checkResult(draggedTile, tileToBeReplaced)

    }

    private fun showPuzzle() {
        puzzleTiles.forEach { puzzleTile ->
            val params = RelativeLayout.LayoutParams(
                puzzleTile.width.toInt(),
                puzzleTile.height.toInt()
            )

            Glide.with(requireActivity()).load(puzzleTile.bitmap).into(puzzleTile)
            binding.layout.addView(puzzleTile, params)

            puzzleTile.setOnTouchListener(object : View.OnTouchListener {

                private var previousX: Float = 0.0f
                private var previousY: Float = 0.0f
                private var deltaX = 0f
                private var deltaY = 0f
                private var direction = -1

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when (event!!.action) {
                        MotionEvent.ACTION_DOWN -> {
                            previousX = view!!.x - event.rawX
                            previousY = view.y - event.rawY
                            deltaX = event.rawX
                            deltaY = event.rawY
                            view.bringToFront()
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = abs(deltaX - event.rawX)
                            val deltaY = abs(deltaY - event.rawY)

                            if (direction < 0) {
                                direction = if (deltaX <= deltaY) {
                                    if ((this.deltaY - event.rawY) < 0) {
                                        DIRECTION_BOTTOM
                                    } else {
                                        DIRECTION_TOP
                                    }
                                } else {
                                    if ((this.deltaX - event.rawX) < 0) {
                                        DIRECTION_RIGHT
                                    } else {
                                        DIRECTION_LEFT
                                    }
                                }
                            }

                            val tile = view as PuzzleTile
                            when (direction) {
                                DIRECTION_LEFT -> {
                                    if (deltaX <= tile.width && tile.canMoveLeft) {
                                        view.animate()
                                            .x(event.rawX + previousX)
                                            .setDuration(0)
                                            .start()
                                    }
                                }

                                DIRECTION_RIGHT -> {
                                    if (deltaX <= tile.width && tile.canMoveRight) {
                                        view.animate()
                                            .x(event.rawX + previousX)
                                            .setDuration(0)
                                            .start()

                                    }
                                }

                                DIRECTION_TOP -> {
                                    if (deltaY <= tile.height && tile.canMoveTop) {
                                        view.animate()
                                            .y(event.rawY + previousY)
                                            .setDuration(0)
                                            .start()
                                    }
                                }
                                DIRECTION_BOTTOM -> {
                                    if (deltaY <= tile.height && tile.canMoveBottom) {
                                        view.animate()
                                            .y(event.rawY + previousY)
                                            .setDuration(0)
                                            .start()
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            val tile = view as PuzzleTile
                            performMovementAction(tile, direction)

                            direction = -1
                        }
                        else -> return false
                    }
                    return true
                }
            })
        }
        binding.simpleChronometer.base = SystemClock.elapsedRealtime()
        binding.simpleChronometer.start()
    }

    private fun checkResult(
        draggedTile: PuzzleTile,
        tileToBeReplaced: PuzzleTile
    ) {
        val id1 = draggedTile.correctPosition
        val id2 = tileToBeReplaced.correctPosition

        if (
            id1 == draggedTile.position
        ) {
            correctItemsIds.add(id1)
        } else {
            correctItemsIds.remove(id1)
        }

        if (
            id2 == tileToBeReplaced.position
        ) {
            correctItemsIds.add(id2)
        } else {
            correctItemsIds.remove(id2)
        }

        if (correctItemsIds.size > pieceNumbers - 2) {
            navigateToCongratsFragment()
        }

    }

    private fun navigateToCongratsFragment() {
        binding.simpleChronometer.stop()
        showConfetti()

        navigationDelay = object : CountDownTimer(3000, 50) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val duration = binding.simpleChronometer.text.removePrefix("Time Running - ")
                findNavController().navigate(
                    NewPuzzleFragmentDirections.actionNewPuzzleFragmentToCongratsFragment(
                        duration.toString()
                    )
                )
            }
        }
        navigationDelay.start()
    }

    private fun showConfetti() {
        binding.newPuzzleFragmentConfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(2f, 10f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .setPosition(+60f, binding.newPuzzleFragmentConfetti.width + 50f, -50f, 100f)
            .streamFor(300, 3000L)
    }

    override fun onDestroy() {
        try {
            previewTimer.cancel()
            navigationDelay.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    companion object {
        const val DIRECTION_LEFT = 1
        const val DIRECTION_TOP = 2
        const val DIRECTION_RIGHT = 3
        const val DIRECTION_BOTTOM = 4
    }
}