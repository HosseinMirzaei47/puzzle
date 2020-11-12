package com.example.puzzleapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.puzzleapp.databinding.FragmentPuzzleGameBinding
import kotlin.math.sqrt

class PuzzleGameFragment : Fragment(R.layout.fragment_puzzle_game), AdapterCallbacks {

    private lateinit var binding: FragmentPuzzleGameBinding
    private val args: PuzzleGameFragmentArgs by navArgs()

    private val puzzlePieces = mutableListOf<PuzzlePiece>()
    private val controller = Controller(this)
    private var pieceNumbers = Levels.LEVEL_EASY /*Default value*/

    private var anItemIsSelected = false
    private var firstSelectedPiecePosition: Int = -1

    private val correctItemsIds = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pieceNumbers = args.difficulty

        binding = FragmentPuzzleGameBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmapToSplit = BitmapFactory.decodeResource(resources, R.drawable.scarlett_johansson)
        splitImage(bitmapToSplit, pieceNumbers)

        showPuzzle()
    }

    override fun onPieceClicked(position: Int, id: Int) {
        if (!anItemIsSelected) {
            anItemIsSelected = true
            firstSelectedPiecePosition = position
        } else {
            compareSelectedPieces(position)
        }
    }

    private fun splitImage(image: Bitmap, pieceNumbers: Int) {
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
        puzzlePieces.shuffle()

        (0 until puzzlePieces.size).forEach { index ->
            val element = puzzlePieces[index].id
            if (index == element) {
                correctItemsIds.add(element)
            }
        }

        println("jalil ${correctItemsIds.size}")
    }

    private fun compareSelectedPieces(secondSelectedPiecePosition: Int) {

        checkResult(secondSelectedPiecePosition)

        val previousPiece = puzzlePieces[firstSelectedPiecePosition]
        val currentPiece = puzzlePieces[secondSelectedPiecePosition]

        puzzlePieces[firstSelectedPiecePosition] = currentPiece
        puzzlePieces[secondSelectedPiecePosition] = previousPiece

        controller.setData(puzzlePieces)

        anItemIsSelected = false
    }

    private fun checkResult(secondSelectedPiecePosition: Int) {
        val id1 = puzzlePieces[firstSelectedPiecePosition].id
        val id2 = puzzlePieces[secondSelectedPiecePosition].id

        if (
            id1 == secondSelectedPiecePosition &&
            firstSelectedPiecePosition != secondSelectedPiecePosition
        ) {
            correctItemsIds.add(id1)
            if (id2 == firstSelectedPiecePosition) {
                correctItemsIds.add(id2)
            } else {
                correctItemsIds.remove(id2)
            }

        } else if (
            id2 == firstSelectedPiecePosition &&
            firstSelectedPiecePosition != secondSelectedPiecePosition
        ) {
            correctItemsIds.add(id2)
            if (id1 == secondSelectedPiecePosition) {
                correctItemsIds.add(id1)
            } else {
                correctItemsIds.remove(id1)
            }

        }

        if (correctItemsIds.size > pieceNumbers - 2) {
            println("jalil bordi berar base dege")
        }
    }

    private fun showPuzzle() {
        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            setController(controller)
        }
        controller.setData(puzzlePieces)
    }

}
