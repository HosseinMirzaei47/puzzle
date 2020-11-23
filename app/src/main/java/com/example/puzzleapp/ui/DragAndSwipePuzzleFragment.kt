package com.example.puzzleapp.ui

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.puzzleapp.databinding.FragmentPuzzleDragSwipeBinding
import com.example.puzzleapp.models.PuzzlePiece
import com.example.puzzleapp.utils.Settings
import com.example.puzzleapp.utils.getBitmapPositionInsideImageView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class DragAndSwipePuzzleFragment : Fragment() {

    private lateinit var binding: FragmentPuzzleDragSwipeBinding
    private val args: DragAndSwipePuzzleFragmentArgs by navArgs()

    @Inject
    lateinit var settings: Settings
    private lateinit var navigationDelay: CountDownTimer
    private lateinit var previewTimer: CountDownTimer

    private lateinit var unScramblePuzzleJob: Job

    private val pieceNumbers by lazy { args.difficulty }
    private val puzzleMode by lazy { args.puzzleMode }

    private val correctItemsIds = mutableSetOf<Int>()
    private val puzzlePieces = arrayListOf<PuzzlePiece>()

    private var backPressedMills = -1L
    private var gameIsOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (System.currentTimeMillis() - backPressedMills < 2000) {
                findNavController().navigateUp()
            } else {
                backPressedMills = System.currentTimeMillis()
                Snackbar.make(requireView(), "Press again to exit", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPuzzleDragSwipeBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                isPlaying = false
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnHintClicks()

        getAndDisplayPuzzle()
    }

    private fun getAndDisplayPuzzle() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            settings.puzzleType.collect { srcType ->
                if (srcType == Settings.TYPE_DEFAULT) {
                    settings.puzzleSrcDrawable.collect { src ->
                        val puzzleSrc =
                            BitmapFactory.decodeResource(resources, src)
                        binding.puzzleSrc = puzzleSrc
                        createPreviewCountDown(puzzleSrc)
                    }
                } else if (srcType == Settings.TYPE_CUSTOM) {
                    settings.puzzleSrcPath.collect { puzzleSrc ->
                        val imageToSplit = BitmapFactory.decodeFile(puzzleSrc)
                        binding.puzzleSrc = imageToSplit
                        createPreviewCountDown(imageToSplit)
                    }
                }
            }
        }
    }

    private suspend fun createPreviewCountDown(puzzleSrc: Bitmap) {
        withContext(Dispatchers.Main) { binding.progressAnimation.visibility = View.GONE }
        viewLifecycleOwner.lifecycleScope.launch {
            previewTimer = object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val previewTimer = "Preview timer: ${(millisUntilFinished / 1000)}"
                    binding.tvCountDown.text = previewTimer
                }

                override fun onFinish() {
                    configureViewParamsAndShowPuzzle(puzzleSrc)
                }
            }
            previewTimer.start()
        }
    }

    private fun configureViewParamsAndShowPuzzle(puzzleSrc: Bitmap) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (puzzleMode == LevelFragment.MODE_DRAG) {
                binding.showHintButton.isVisible = true
            } else {
                val layoutParams =
                    binding.passLevelButton.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                binding.passLevelButton.layoutParams = layoutParams
            }
            binding.isPlaying = true
            binding.previewVisibility = View.GONE
            splitImage(puzzleSrc, pieceNumbers)
        }
    }

    private suspend fun splitImage(puzzleSrc: Bitmap, pieceNumbers: Int) {
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
            Bitmap.createScaledBitmap(puzzleSrc, scaledBitmapWidth, scaledBitmapHeight, true)
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
                val bitmap = Bitmap.createBitmap(
                    croppedBitmap,
                    xCoord,
                    yCoord,
                    pieceWidth,
                    pieceHeight
                )

                val canvas = Canvas(bitmap)
                val path = Path()
                path.moveTo(0.1f, 0.1f)
                path.lineTo(pieceWidth.toFloat(), 1f)
                path.lineTo(pieceWidth.toFloat(), pieceHeight.toFloat())
                path.lineTo(1f, pieceHeight.toFloat())
                path.close()

                // draw a white border
                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 10.0f
                canvas.drawPath(path, border)

                // draw a black border
                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 5.0f
                canvas.drawPath(path, border)


                val piece = PuzzlePiece(
                    requireContext(),
                    pieceWidth.toFloat(),
                    pieceHeight.toFloat()
                )
                piece.correctPoint = PointF(
                    (xCoord + binding.imageView.left).toFloat(),
                    (yCoord + binding.imageView.top).toFloat()
                )
                piece.correctPosition = id++
                piece.bitmap = bitmap
                puzzlePieces.add(piece)
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }

        val listOfPoints = arrayListOf<PointF>()
        puzzlePieces.forEachIndexed { _, puzzlePieces ->
            puzzlePieces.correctPoint.let { pointF -> listOfPoints.add(pointF) }
        }
        val previousFistIndex = puzzlePieces[0]
        while (puzzlePieces[0] == previousFistIndex) {
            puzzlePieces.shuffle()
        }
        puzzlePieces.forEachIndexed { index, puzzlePieces ->
            puzzlePieces.currentPoint = listOfPoints[index]
            puzzlePieces.x = listOfPoints[index].x
            puzzlePieces.y = listOfPoints[index].y
            puzzlePieces.canMoveLeft = canMove(index, DIRECTION_LEFT, pieceNumbers)
            puzzlePieces.canMoveTop = canMove(index, DIRECTION_TOP, pieceNumbers)
            puzzlePieces.canMoveRight = canMove(index, DIRECTION_RIGHT, pieceNumbers)
            puzzlePieces.canMoveBottom = canMove(index, DIRECTION_BOTTOM, pieceNumbers)
            puzzlePieces.position = index
        }
        (0 until puzzlePieces.size).forEach { index ->
            val piece = puzzlePieces[index]
            if (piece.correctPosition == piece.position) {
                correctItemsIds.add(piece.correctPosition)
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

    private fun performMovementAction(draggedPiece: PuzzlePiece, direction: Int) {
        val pieceToBeReplaced: PuzzlePiece?

        when (direction) {
            DIRECTION_LEFT -> {
                if (!draggedPiece.canMoveLeft) {
                    return
                }
                pieceToBeReplaced = puzzlePieces[draggedPiece.position - 1]
                replacePieces(draggedPiece, pieceToBeReplaced)
            }

            DIRECTION_RIGHT -> {
                if (!draggedPiece.canMoveRight) {
                    return
                }
                pieceToBeReplaced = puzzlePieces[draggedPiece.position + 1]
                replacePieces(draggedPiece, pieceToBeReplaced)
            }

            DIRECTION_TOP -> {
                if (!draggedPiece.canMoveTop) {
                    return
                }
                pieceToBeReplaced =
                    puzzlePieces[draggedPiece.position - sqrt(pieceNumbers.toDouble()).toInt()]
                replacePieces(draggedPiece, pieceToBeReplaced)
            }

            else -> {
                if (!draggedPiece.canMoveBottom) {
                    return
                }
                pieceToBeReplaced =
                    puzzlePieces[draggedPiece.position + sqrt(pieceNumbers.toDouble()).toInt()]
                replacePieces(draggedPiece, pieceToBeReplaced)
            }
        }

        animateToCorrectPosition(pieceToBeReplaced)
        animateToCorrectPosition(draggedPiece)
    }

    private fun replacePieces(
        draggedPiece: PuzzlePiece,
        pieceToBeReplaced: PuzzlePiece
    ) {
        puzzlePieces[pieceToBeReplaced.position] = draggedPiece
        puzzlePieces[draggedPiece.position] = pieceToBeReplaced

        val tempPositionHolder = draggedPiece.position
        draggedPiece.position = pieceToBeReplaced.position
        pieceToBeReplaced.position = tempPositionHolder

        val tempPointHolder = draggedPiece.currentPoint
        draggedPiece.currentPoint = pieceToBeReplaced.currentPoint
        pieceToBeReplaced.currentPoint = tempPointHolder

        val tempCanMoveLeftHolder = draggedPiece.canMoveLeft
        draggedPiece.canMoveLeft = pieceToBeReplaced.canMoveLeft
        pieceToBeReplaced.canMoveLeft = tempCanMoveLeftHolder

        val tempCanMoveTopHolder = draggedPiece.canMoveTop
        draggedPiece.canMoveTop = pieceToBeReplaced.canMoveTop
        pieceToBeReplaced.canMoveTop = tempCanMoveTopHolder

        val tempCanMoveRightHolder = draggedPiece.canMoveRight
        draggedPiece.canMoveRight = pieceToBeReplaced.canMoveRight
        pieceToBeReplaced.canMoveRight = tempCanMoveRightHolder

        val tempCanMoveBottomHolder = draggedPiece.canMoveBottom
        draggedPiece.canMoveBottom = pieceToBeReplaced.canMoveBottom
        pieceToBeReplaced.canMoveBottom = tempCanMoveBottomHolder

        checkResult(draggedPiece, pieceToBeReplaced)

    }

    private fun showPuzzle() {
        puzzlePieces.forEach { puzzlePiece ->
            val params = RelativeLayout.LayoutParams(
                puzzlePiece.width.toInt(),
                puzzlePiece.height.toInt()
            )

            Glide.with(requireActivity()).load(puzzlePiece.bitmap).into(puzzlePiece)
            binding.layout.addView(puzzlePiece, params)
            animateToCorrectPosition(puzzlePiece)

            if (puzzleMode == LevelFragment.MODE_SWIPE) {
                puzzlePiece.setOnTouchListener(object : View.OnTouchListener {

                    private var previousX: Float = 0.0f
                    private var previousY: Float = 0.0f
                    private var firstRawX = 0f
                    private var firstRawY = 0f
                    private var direction = -1

                    @SuppressLint("ClickableViewAccessibility")
                    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                        when (event!!.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (gameIsOver) {
                                    return false
                                }
                                previousX = view!!.x - event.rawX
                                previousY = view.y - event.rawY
                                firstRawX = event.rawX
                                firstRawY = event.rawY
                                view.bringToFront()
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val deltaX = abs(firstRawX - event.rawX)
                                val deltaY = abs(firstRawY - event.rawY)
                                val rawDeltaX = firstRawX - event.rawX
                                val rawDeltaY = firstRawY - event.rawY

                                val piece = view as PuzzlePiece

                                if (direction < 0) {
                                    direction =
                                        detectDirection(deltaX, deltaY, firstRawX, firstRawY, event)
                                } else if (direction != -1) {
                                    if (direction == DIRECTION_RIGHT || direction == DIRECTION_LEFT) {
                                        if (abs(firstRawX - event.rawX) < 85) {
                                            direction = -1
                                            animateToCorrectPosition(piece)
                                            return false
                                        }
                                    } else {
                                        if (abs(firstRawY - event.rawY) < 85) {
                                            direction = -1
                                            animateToCorrectPosition(piece)
                                            return false
                                        }
                                    }
                                }

                                when (direction) {
                                    DIRECTION_LEFT -> {
                                        if (deltaX <= piece.width && piece.canMoveLeft && rawDeltaX > 0) {
                                            view.animate()
                                                .x(event.rawX + previousX)
                                                .setDuration(0)
                                                .start()
                                        }
                                    }

                                    DIRECTION_RIGHT -> {
                                        if (deltaX <= piece.width && piece.canMoveRight && rawDeltaX < 0) {
                                            view.animate()
                                                .x(event.rawX + previousX)
                                                .setDuration(0)
                                                .start()

                                        }
                                    }

                                    DIRECTION_TOP -> {
                                        if (deltaY <= piece.height && piece.canMoveTop && rawDeltaY > 0) {
                                            view.animate()
                                                .y(event.rawY + previousY)
                                                .setDuration(0)
                                                .start()
                                        }
                                    }
                                    DIRECTION_BOTTOM -> {
                                        if (deltaY <= piece.height && piece.canMoveBottom && rawDeltaY < 0) {
                                            view.animate()
                                                .y(event.rawY + previousY)
                                                .setDuration(0)
                                                .start()
                                        }
                                    }
                                }
                            }

                            MotionEvent.ACTION_UP -> {
                                val piece = view as PuzzlePiece
                                if (direction == DIRECTION_TOP || direction == DIRECTION_BOTTOM) {
                                    if (abs(firstRawY - event.rawY) < piece.height / 4) {
                                        animateToCorrectPosition(piece)
                                        direction = -1
                                        return false
                                    }
                                } else {
                                    if (abs(firstRawX - event.rawX) < piece.width / 4) {
                                        animateToCorrectPosition(piece)
                                        direction = -1
                                        return false
                                    }
                                }
                                performMovementAction(piece, direction)
                                direction = -1
                            }
                            else -> return false
                        }
                        return true
                    }
                })
            } else if (puzzleMode == LevelFragment.MODE_DRAG) {
                puzzlePiece.setOnTouchListener(object : View.OnTouchListener {

                    private var previousX: Float = 0.0f
                    private var previousY: Float = 0.0f
                    private var firstRawX = 0f
                    private var firstRawY = 0f

                    @SuppressLint("ClickableViewAccessibility")
                    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                        when (event!!.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (gameIsOver) {
                                    return false
                                }
                                previousX = view!!.x - event.rawX
                                previousY = view.y - event.rawY
                                firstRawX = event.rawX
                                firstRawY = event.rawY
                                view.bringToFront()
                            }

                            MotionEvent.ACTION_MOVE -> {
                                view!!.animate()
                                    .x(event.rawX + previousX)
                                    .y(event.rawY + previousY)
                                    .setDuration(0)
                                    .start()
                            }

                            MotionEvent.ACTION_UP -> {
                                val piece = view as PuzzlePiece
                                replaceWithNearestPiece(piece)
                            }
                            else -> return false
                        }
                        return true
                    }
                })
            }

        }
        binding.simpleChronometer.base = SystemClock.elapsedRealtime()
        binding.simpleChronometer.start()
    }

    private fun replaceWithNearestPiece(piece: PuzzlePiece) {
        var nearestPiece: PuzzlePiece = piece
        var nearestHypotenuseValue: Double = Double.MAX_VALUE
        puzzlePieces.forEachIndexed { _, puzzlePiece ->
            val deltaX = abs(puzzlePiece.currentPoint!!.x - piece.x)
            val deltaY = abs(puzzlePiece.currentPoint!!.y - piece.y)

            val hypotenuse = sqrt(deltaX.toDouble().pow(2.0) + deltaY.toDouble().pow(2.0))
            if (hypotenuse < nearestHypotenuseValue) {
                nearestPiece = puzzlePiece
                nearestHypotenuseValue = hypotenuse
            }
        }

        if (piece != nearestPiece) {
            replacePieces(piece, nearestPiece)
            animateToCorrectPosition(piece)
            animateToCorrectPosition(nearestPiece)
        } else {
            animateToCorrectPosition(piece)
        }
    }

    fun animateToCorrectPosition(piece: PuzzlePiece) {
        piece.animate()
            .x(piece.currentPoint!!.x)
            .y(piece.currentPoint!!.y)
            .setDuration(100)
            .start()
    }

    private fun checkResult(
        draggedPiece: PuzzlePiece,
        pieceToBeReplaced: PuzzlePiece
    ) {
        val id1 = draggedPiece.correctPosition
        val id2 = pieceToBeReplaced.correctPosition

        if (
            id1 == draggedPiece.position
        ) {
            correctItemsIds.add(id1)
        } else {
            correctItemsIds.remove(id1)
        }

        if (
            id2 == pieceToBeReplaced.position
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
        gameIsOver = true
        binding.simpleChronometer.stop()
        showConfetti()

        navigationDelay = object : CountDownTimer(3000, 50) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                val duration = binding.simpleChronometer.text.removePrefix("Time Running - ")
                findNavController().navigate(
                    DragAndSwipePuzzleFragmentDirections.actionNewPuzzleFragmentToCongratsFragment(
                        duration.toString()
                    )
                )
            }
        }
        navigationDelay.start()
    }

    fun detectDirection(
        deltaX: Float, deltaY: Float,
        firstRawX: Float, firstRawY: Float,
        event: MotionEvent
    ): Int {
        return if (deltaX <= deltaY) {
            if ((firstRawY - event.rawY) < 0) {
                DIRECTION_BOTTOM
            } else {
                DIRECTION_TOP
            }
        } else {
            if ((firstRawX - event.rawX) < 0) {
                DIRECTION_RIGHT
            } else {
                DIRECTION_LEFT
            }
        }
    }

    private fun setOnHintClicks() {
        binding.showHintButton.setOnClickListener { giveAHint() }
        binding.passLevelButton.setOnClickListener { passLevel() }
        binding.btnSkipCorrection.setOnClickListener {
            unScramblePuzzleJob.cancel()
            do {
                puzzlePieces.forEachIndexed { _, piece ->
                    if (!correctItemsIds.contains(piece.position)) {
                        val nearestPiece = puzzlePieces[piece.position]
                        replacePieces(piece, nearestPiece)
                        animateToCorrectPosition(piece)
                        animateToCorrectPosition(nearestPiece)
                    }
                }
            } while (correctItemsIds.size < pieceNumbers - 1)
        }
    }

    private fun giveAHint() {
        if (puzzleMode == LevelFragment.MODE_DRAG) {
            while (correctItemsIds.size < pieceNumbers - 1) {
                val randomNumber = Random().nextInt(puzzlePieces.size - 1)
                val piece = puzzlePieces[randomNumber]
                if (!correctItemsIds.contains(piece.correctPosition)) {
                    val nearestPiece = puzzlePieces[piece.correctPosition]
                    replacePieces(piece, nearestPiece)
                    animateToCorrectPosition(piece)
                    animateToCorrectPosition(nearestPiece)
                    break
                }
            }
        }
    }

    private fun passLevel() {
        gameIsOver = true
        if (puzzleMode == LevelFragment.MODE_DRAG) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                do {
                    puzzlePieces.forEachIndexed { _, piece ->
                        if (!correctItemsIds.contains(piece.correctPosition)) {
                            delay(50)
                            val nearestPiece = puzzlePieces[piece.correctPosition]
                            withContext(Dispatchers.Main) {
                                replacePieces(piece, nearestPiece)
                                animateToCorrectPosition(piece)
                                animateToCorrectPosition(nearestPiece)
                            }
                        }
                    }
                } while (correctItemsIds.size < pieceNumbers - 1)
            }
        } else {
            binding.btnSkipCorrection.visibility = View.VISIBLE
            binding.passLevelButton.visibility = View.GONE
            binding.showHintButton.visibility = View.GONE
            unScramblePuzzleJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                do {
                    puzzlePieces.forEachIndexed { _, piece ->
                        if (!correctItemsIds.contains(piece.position)) {
                            delay(10)
                            when (Random().nextInt(3) + 1) {
                                1 -> {
                                    /**
                                     * For the unscramble algorithm to work, one direction
                                     * scope must be empty
                                     * **/
                                }
                                2 -> {
                                    if (piece.canMoveTop) {
                                        withContext(Dispatchers.Main) {
                                            performMovementAction(piece, DIRECTION_TOP)
                                        }
                                    }
                                }
                                3 -> {
                                    if (piece.canMoveRight) {
                                        withContext(Dispatchers.Main) {
                                            performMovementAction(piece, DIRECTION_RIGHT)
                                        }
                                    }
                                }
                                4 -> {
                                    if (piece.canMoveBottom) {
                                        withContext(Dispatchers.Main) {
                                            performMovementAction(piece, DIRECTION_BOTTOM)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } while (correctItemsIds.size < pieceNumbers - 2)
                puzzlePieces.forEachIndexed { _, piece ->
                    if (!correctItemsIds.contains(piece.correctPosition)) {
                        val nearestPiece = puzzlePieces[piece.correctPosition]
                        withContext(Dispatchers.Main) {
                            replacePieces(piece, nearestPiece)
                            animateToCorrectPosition(piece)
                            animateToCorrectPosition(nearestPiece)
                        }
                    }
                }
            }
        }
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

/*


preview
chronometer
touchCounter
hints


* */
