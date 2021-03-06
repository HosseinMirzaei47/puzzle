package com.example.puzzleapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.puzzleapp.databinding.FragmentPuzzleClickBinding
import com.example.puzzleapp.models.ClickPuzzlePiece
import com.example.puzzleapp.utils.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class ClickPuzzleFragment : Fragment() {

    private lateinit var binding: FragmentPuzzleClickBinding
    private val args: ClickPuzzleFragmentArgs by navArgs()

    private lateinit var pieceAdapter: PieceAdapter

    @Inject
    lateinit var settings: Settings
    private lateinit var navigationDelay: CountDownTimer
    private lateinit var previewTimer: CountDownTimer

    private val correctItemsIds = mutableSetOf<Int>()
    private val puzzlePieces = mutableListOf<ClickPuzzlePiece>()
    private val pieceNumbers by lazy { args.difficulty }
    private val puzzleMode by lazy { args.puzzleMode }

    private var gameIsOver = false
    private var anItemIsSelected = false
    private var firstSelectedPiecePosition = Int.MIN_VALUE

    private lateinit var firstSelectedPieceView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPuzzleClickBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            previewVisibility = View.VISIBLE
            isPlaying = false
        }

        binding.recyclerview.recycledViewPool.clear()

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
                        createPreviewCountDown(imageToSplit)
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

        val scaledBitmap = Bitmap.createScaledBitmap(image, image.width, image.height, true)
        val cols = sqrt(pieceNumbers.toDouble()).toInt()
        rows = cols
        pieceHeight = image.height / rows
        pieceWidth = image.width / cols

        var id = 0
        var yCoord = 0
        for (x in 0 until rows) {
            var xCoord = 0
            for (y in 0 until cols) {
                puzzlePieces.add(
                    ClickPuzzlePiece(
                        id++,
                        Bitmap.createBitmap(
                            scaledBitmap,
                            xCoord,
                            yCoord,
                            pieceWidth,
                            pieceHeight
                        )
                    )
                )
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }

        val previousFist = puzzlePieces[0]

        while (puzzlePieces[0] == previousFist) {
            puzzlePieces.shuffle()
        }

        (0 until puzzlePieces.size).forEach { index ->
            val element = puzzlePieces[index].id
            if (index == element) {
                correctItemsIds.add(element)
            }
        }

        withContext(Dispatchers.Main) {
            showPuzzle()
        }

    }

    private fun compareSelectedPieces(secondSelectedPiecePosition: Int) {

        checkResult(secondSelectedPiecePosition)

        val previousPiece = puzzlePieces[firstSelectedPiecePosition]
        val currentPiece = puzzlePieces[secondSelectedPiecePosition]

        puzzlePieces[firstSelectedPiecePosition] = currentPiece
        puzzlePieces[secondSelectedPiecePosition] = previousPiece

        if (gameIsOver) {
            navigateToCongratsFragment()
        }

        pieceAdapter.notifyItemChanged(firstSelectedPiecePosition)
        pieceAdapter.notifyItemChanged(secondSelectedPiecePosition)

        anItemIsSelected = false
    }

    private fun navigateToCongratsFragment() {
        binding.simpleChronometer.stop()
        binding.passLevelButton.visibility = View.GONE
        binding.showHintButton.visibility = View.GONE

        showConfetti()

        navigationDelay = object : CountDownTimer(3000, 50) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val duration = binding.simpleChronometer.text.removePrefix("Time Running - ")
                findNavController().navigate(
                    ClickPuzzleFragmentDirections.actionGameFragmentToCongratsFragment(
                        duration.toString()
                    )
                )
            }
        }
        navigationDelay.start()
    }

    private fun checkResult(secondSelectedPiecePosition: Int) {
        val id1 = puzzlePieces[firstSelectedPiecePosition].id
        val id2 = puzzlePieces[secondSelectedPiecePosition].id

        if (
            id1 == secondSelectedPiecePosition &&
            firstSelectedPiecePosition != secondSelectedPiecePosition
        ) {
            correctItemsIds.add(id1)
        } else {
            correctItemsIds.remove(id1)
        }

        if (
            id2 == firstSelectedPiecePosition &&
            firstSelectedPiecePosition != secondSelectedPiecePosition
        ) {
            correctItemsIds.add(id2)
        } else {
            correctItemsIds.remove(id2)
        }

        if (correctItemsIds.size > pieceNumbers - 2) {
            gameIsOver = true
        }

    }

    private fun showPuzzle() {
        pieceAdapter = PieceAdapter(
            { position: Int, view: View -> onTouchPiece(position, view) },
            puzzleMode == 0
        )
        pieceAdapter.pieces = puzzlePieces
        binding.recyclerview.apply {
            val spanCount = sqrt(pieceNumbers.toDouble()).toInt()
            layoutManager =
                GridLayoutManager(requireContext(), spanCount)
            adapter = pieceAdapter
        }

        binding.simpleChronometer.format = "Time Running - %s"
        binding.simpleChronometer.base = SystemClock.elapsedRealtime()
        binding.simpleChronometer.start()
    }

    private fun showConfetti() {
        binding.gameFragmentConfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(2f, 10f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .setPosition(+60f, binding.gameFragmentConfetti.width + 50f, -50f, 100f)
            .streamFor(300, 3000L)
    }

    private fun setOnHintClicks() {
        binding.showHintButton.setOnClickListener { giveAHint() }
        binding.passLevelButton.setOnClickListener { passLevel() }
    }

    private fun giveAHint() {
        puzzlePieces.forEachIndexed { index, piece ->
            if (!correctItemsIds.contains(piece.id)) {
                val id = piece.id
                firstSelectedPiecePosition = id
                compareSelectedPieces(index)
                return
            }
        }
    }

    private fun passLevel() {
        do {
            puzzlePieces.forEachIndexed { index, piece ->
                if (!correctItemsIds.contains(piece.id)) {
                    val id = piece.id
                    firstSelectedPiecePosition = id
                    compareSelectedPieces(index)
                }
            }
        } while (correctItemsIds.size <= pieceNumbers - 2)
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

    private fun onTouchPiece(position: Int, view: View) {
        if (!gameIsOver) {
            if (!anItemIsSelected) {
                anItemIsSelected = true
                firstSelectedPiecePosition = position
                firstSelectedPieceView = view

                view.scaleX = .8f
                view.scaleY = .8f

            } else {
                firstSelectedPieceView.scaleX = 1f
                firstSelectedPieceView.scaleY = 1f

                compareSelectedPieces(position)
            }
        }
    }
}