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
import androidx.activity.addCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.puzzleapp.databinding.FragmentJigsawBinding
import com.example.puzzleapp.models.JigsawPiece
import com.example.puzzleapp.utils.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class JigsawFragment : Fragment(), OnJigsawPiece {

    private lateinit var binding: FragmentJigsawBinding
    private val args: JigsawFragmentArgs by navArgs()

    private var pieces = listOf<JigsawPiece>()
    private val difficulty by lazy { args.difficulty }
    private var correctPiecesCount = 0

    private lateinit var navigationDelay: CountDownTimer
    private lateinit var previewTimer: CountDownTimer

    private lateinit var imageToSplit: Bitmap
    private var backPressedMills = -1L

    @Inject
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (System.currentTimeMillis() - backPressedMills < 1000) {
                findNavController().navigateUp()
            } else {
                backPressedMills = System.currentTimeMillis()
                Snackbar.make(requireView(), "Press again to exit", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentJigsawBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            previewVisibility = View.VISIBLE
            isPlaying = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showHintButton.setOnClickListener {
            giveAHint()
        }
        binding.passLevelButton.setOnClickListener {
            passLevel()
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            settings.puzzleType.collect { srcType ->
                if (srcType == Settings.TYPE_DEFAULT) {
                    settings.puzzleSrcDrawable.collect { puzzleSrc ->
                        imageToSplit = BitmapFactory.decodeResource(resources, puzzleSrc)
                        binding.puzzleSrc = imageToSplit
                        createPreviewCountDown()
                    }
                } else if (srcType == Settings.TYPE_CUSTOM) {
                    settings.puzzleSrcPath.collect { puzzleSrc ->
                        imageToSplit = BitmapFactory.decodeFile(puzzleSrc)
                        binding.puzzleSrc = imageToSplit
                        // createPreviewCountDown()

                    }
                }
            }
        }
    }

    private suspend fun createJigsaw() {
        pieces = splitImage(requireContext(), binding.imageView, difficulty)
        val bitmap = splitImage1(requireContext(), binding.imageView, difficulty)
        binding.puzzleSrc = bitmap
        val touchListener = TouchListener(this)
        /** DataBinding needs a little bit of time to set buttons visibility
         * we perform the operation with 100 delay so that the pieces positions
         * on the screen that are dependent on buttons dimensions are set.
         * **/
        delay(100)
        pieces = pieces.shuffled()
        withContext(Main) {
            for (piece in pieces) {
                piece.setOnTouchListener(touchListener)
                binding.layout.addView(piece)

                piece.y = (binding.layout.height - piece.pieceHeight).toFloat()
                piece.x =
                    java.util.Random().nextInt(binding.layout.width - piece.pieceWidth).toFloat()
                /*val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin = Random().nextInt(binding.layout.width - piece.pieceWidth)
                lParams.topMargin = binding.layout.height - piece.pieceHeight
                piece.layoutParams = lParams*/
            }
        }
    }

    private suspend fun createPreviewCountDown() {
        withContext(Main) { binding.progressAnimation.visibility = View.GONE }
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
                        createJigsaw()
                        binding.simpleChronometer.base = SystemClock.elapsedRealtime()
                        binding.simpleChronometer.start()
                    }
                }
            }
            previewTimer.start()
        }
    }

    private fun giveAHint() {
        pieces.forEach { piece ->
            if (piece.canMove) {
                piece.animate()
                    .x(piece.xCoord.toFloat())
                    .y(piece.yCoord.toFloat())
                    .rotationBy(360f)
                    .setDuration(900)
                    .start()
                piece.canMove = false
                onJigsawPiece(piece)
                return
            }
        }
    }

    private fun passLevel() {
        pieces.forEachIndexed { index, piece ->
            if (piece.canMove) {
                piece.animate()
                    .setStartDelay((50 * index).toLong())
                    .x(piece.xCoord.toFloat())
                    .y(piece.yCoord.toFloat())
                    .rotationBy(if (index % 2 == 0) 360f else -360f)
                    .setDuration(1500)
                    .start()
                piece.canMove = false
                onJigsawPiece(piece)
            }
        }
    }

    private fun navigateToCongratsFragment() {
        binding.simpleChronometer.stop()
        showConfetti()

        navigationDelay = object : CountDownTimer(4000, 50) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val duration = binding.simpleChronometer.text.removePrefix("Time Running - ")
                findNavController().navigate(
                    JigsawFragmentDirections.actionJigsawFragmentToCongratsFragment(duration.toString())
                )
            }
        }
        navigationDelay.start()
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

    override fun onJigsawPiece(jigsawPiece: JigsawPiece) {
        correctPiecesCount++
        if (correctPiecesCount == difficulty) {
            navigateToCongratsFragment()
        }
    }

    override fun onDestroy() {
        try {
            pieces.forEachIndexed { _, puzzle ->
                puzzle.drawable.toBitmap().recycle()
            }
            previewTimer.cancel()
            navigationDelay.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}