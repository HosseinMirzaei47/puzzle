package com.example.puzzleapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.puzzleapp.ItemTouchHelperDrag
import com.example.puzzleapp.ItemTouchHelperSwipe
import com.example.puzzleapp.OnTouchPuzzleTile
import com.example.puzzleapp.databinding.FragmentGameBinding
import com.example.puzzleapp.models.PuzzlePiece
import com.example.puzzleapp.utils.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class GameFragment : Fragment(), OnTouchPuzzleTile {

    private lateinit var binding: FragmentGameBinding
    private val args: GameFragmentArgs by navArgs()

    private lateinit var pieceAdapter: PieceAdapter

    @Inject
    lateinit var settings: Settings
    private val controller = Controller()
    private lateinit var navigationDelay: CountDownTimer
    private lateinit var previewTimer: CountDownTimer

    private val correctItemsIds = mutableSetOf<Int>()
    private val puzzlePieces = mutableListOf<PuzzlePiece>()
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

        binding = FragmentGameBinding.inflate(
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
                    PuzzlePiece(
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
        showConfetti()

        navigationDelay = object : CountDownTimer(3000, 50) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val duration = binding.simpleChronometer.text.removePrefix("Time Running - ")
                findNavController().navigate(
                    GameFragmentDirections.actionGameFragmentToCongratsFragment(
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
        pieceAdapter = PieceAdapter(this, puzzleMode == 0)
        pieceAdapter.pieces = puzzlePieces
        binding.recyclerview.apply {
            val spanCount = sqrt(pieceNumbers.toDouble()).toInt()
            layoutManager =
                GridLayoutManager(requireContext(), spanCount)
            adapter = pieceAdapter
            /*setController(controller)*/
        }

        when (puzzleMode) {
            1 -> {
                val itemTouchHelperDrag = ItemTouchHelperDrag(this)
                itemTouchHelperDrag.attachToRecyclerView(binding.recyclerview)
            }
            2 -> {
                val itemTouchHelperVerticalSwipe =
                    ItemTouchHelperSwipe(this, ItemTouchHelper.UP or ItemTouchHelper.DOWN)
                val itemTouchHelperHorizontalSwipe =
                    ItemTouchHelperSwipe(this, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)
                itemTouchHelperVerticalSwipe.attachToRecyclerView(binding.recyclerview)
                itemTouchHelperHorizontalSwipe.attachToRecyclerView(binding.recyclerview)
            }
        }

        binding.simpleChronometer.format = "Time Running - %s"
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

    override fun onDestroy() {
        try {
            previewTimer.cancel()
            navigationDelay.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun moveItem(oldPos: Int, newPos: Int) {
        val temp: PuzzlePiece = puzzlePieces[oldPos]
        puzzlePieces[oldPos] = puzzlePieces[newPos]
        puzzlePieces[newPos] = temp
        pieceAdapter.notifyItemChanged(newPos)
        pieceAdapter.notifyItemChanged(oldPos)
        //recyclerviewAdapter.list= puzzlePieces as ArrayList<PuzzlePiece>
        //recyclerviewAdapter.notifyItemMoved(oldPos, newPos)
        //recyclerviewAdapter.submitList(puzzlePieces)
        //recyclerviewAdapter.notifyDataSetChanged()
    }

    /*==========    ON RECYCLER ITEMS TOUCH INTERFACES IMPLEMENTATION      ==========*/

    override fun onMoveTile(oldPos: Int, newPos: Int) {
        firstSelectedPiecePosition = oldPos
        compareSelectedPieces(newPos)
        // moveItem(oldPos, newPos)
    }

    override fun onSwipeTile(oldPos: Int, newPos: Int) {
        firstSelectedPiecePosition = oldPos
        compareSelectedPieces(newPos)

    }

    override fun onPieceClicked(position: Int, id: Int, view: View) {
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

    override fun onDragViewHolder(viewHolder: RecyclerView.ViewHolder) {}

}