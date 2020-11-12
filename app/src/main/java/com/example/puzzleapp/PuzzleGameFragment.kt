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

    private val puzzlePieces = arrayListOf<Tile>()
    private val controller = Controller(this)
    private var pieceNumbers = Levels.LEVEL_EASY /*Default value*/

    private var anItemIsSelected = true /*Is set to -1 when second piece of puzzle is clicked*/
    private var lastselectedindex: Int = -1

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

        val bitmapToSplit = BitmapFactory.decodeResource(resources, R.drawable.puzzle_image)
        splitImage(bitmapToSplit, pieceNumbers)

        showPuzzle()
    }

    override fun onPieceClicked(position: Int, id: Int) {
        if (anItemIsSelected) {
            anItemIsSelected = false
            lastselectedindex = position
        } else {
            compareSelectedTile(position)
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
                    Tile(
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

    }

    private fun compareSelectedTile(position: Int) {
        val previousPiece = puzzlePieces[position]
        val currentPiece = puzzlePieces[lastselectedindex]

        puzzlePieces[position] = currentPiece
        puzzlePieces[lastselectedindex] = previousPiece

        controller.setData(puzzlePieces)

        anItemIsSelected = true
    }

    private fun showPuzzle() {
        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            setController(controller)
        }
        controller.setData(puzzlePieces)
    }

}
