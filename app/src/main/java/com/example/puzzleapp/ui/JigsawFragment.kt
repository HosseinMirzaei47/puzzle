package com.example.puzzleapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.puzzleapp.R
import com.example.puzzleapp.TouchListener
import com.example.puzzleapp.databinding.FragmentJigsawBinding
import com.example.puzzleapp.models.JigsawPiece
import com.example.puzzleapp.utils.Settings
import com.example.puzzleapp.utils.splitImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class JigsawFragment : Fragment() {

    private lateinit var binding: FragmentJigsawBinding
    private val args: JigsawFragmentArgs by navArgs()

    private var pieces = listOf<JigsawPiece>()
    private val difficulty by lazy { args.difficulty }

    private lateinit var imageToSplit: Bitmap

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentJigsawBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

/*        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            settings.puzzleType.collect { srcType ->
                if (srcType == Settings.TYPE_DEFAULT) {
                    settings.puzzleSrcDrawable.collect { puzzleSrc ->
                        imageToSplit =
                            BitmapFactory.decodeResource(resources, puzzleSrc)
                        binding.imageSrc = imageToSplit
                    }
                } else if (srcType == Settings.TYPE_CUSTOM) {
                    settings.puzzleSrcPath.collect { puzzleSrc ->
                        imageToSplit = BitmapFactory.decodeFile(puzzleSrc)
                        binding.imageSrc = imageToSplit
                    }
                }
                doIt()
            }
        }*/

        imageToSplit =
            BitmapFactory.decodeResource(resources, R.drawable.taylor)
        // binding.imageView.setImageDrawable(requireActivity().getDrawable(R.drawable.scarlett_johansson))
        binding.imageView.setImageBitmap(imageToSplit)
        // binding.imageSrc = imageToSplit
        doIt()
    }

    private fun doIt() {
        binding.imageView.post {
            println("jalil marget biad")
            pieces = splitImage(requireContext(), binding.imageView, 4)
            val touchListener = TouchListener()

            pieces = pieces.shuffled()
            for (piece in pieces) {
                piece.setOnTouchListener(touchListener)
                binding.layout.addView(piece)

                val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin = Random().nextInt(binding.layout.width - piece.pieceWidth)
                lParams.topMargin = binding.layout.height - piece.pieceHeight
                piece.layoutParams = lParams
            }
        }
    }

}