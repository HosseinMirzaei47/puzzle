package com.example.puzzleapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.puzzleapp.databinding.FragmentGameBinding
import kotlin.math.sqrt

class GameFragment : Fragment(R.layout.fragment_game), AdapterCallbacks {

    private lateinit var binding: FragmentGameBinding
    private val args: GameFragmentArgs by navArgs()

    private val controller = Controller(this)

    private val puzzlePieces = mutableListOf<PuzzlePiece>()
    private val pieceNumbers by lazy { args.difficulty }
    private val puzzleSrc by lazy { args.puzzleSrc }

    private var anItemIsSelected = false
    private var firstSelectedPiecePosition: Int = -1

    private val correctItemsIds = mutableSetOf<Int>()

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
            imageSrc = puzzleSrc
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmapToSplit = BitmapFactory.decodeResource(resources, puzzleSrc)
        splitImage(bitmapToSplit, pieceNumbers)

        showPuzzle()

    }

    override fun onPieceClicked(position: Int, id: Int, view: View) {
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
            Toast.makeText(requireContext(), "You won", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPuzzle() {
        binding.recyclerview.apply {
            val spanCount = sqrt(pieceNumbers.toDouble()).toInt()
            layoutManager =
                GridLayoutManager(requireContext(), spanCount)
            setController(controller)
        }
        controller.setData(puzzlePieces)
    }

}
