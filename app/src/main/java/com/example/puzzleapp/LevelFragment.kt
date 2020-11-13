package com.example.puzzleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.puzzleapp.databinding.FragmentLevelBinding

class LevelFragment : Fragment(R.layout.fragment_level) {

    private lateinit var binding: FragmentLevelBinding
    private val args: LevelFragmentArgs by navArgs()

    private val imageSource by lazy {
        args.puzzleSrc
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLevelBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onEasyLevelClick()
        onNormalLevelClick()
        onHardLevelClick()
    }

    private fun onHardLevelClick() {
        binding.levelHardButton.setOnClickListener {
            findNavController().navigate(
                LevelFragmentDirections.actionLevelFragmentToPuzzleGameFragment(
                    Levels.LEVEL_HARD,
                    imageSource
                )
            )
        }
    }

    private fun onNormalLevelClick() {
        binding.levelNormalButton.setOnClickListener {
            findNavController().navigate(
                LevelFragmentDirections.actionLevelFragmentToPuzzleGameFragment(
                    Levels.LEVEL_NORMAL,
                    imageSource
                )
            )
        }
    }

    private fun onEasyLevelClick() {
        binding.levelEasyButton.setOnClickListener {
            findNavController().navigate(
                LevelFragmentDirections.actionLevelFragmentToPuzzleGameFragment(
                    Levels.LEVEL_EASY,
                    imageSource
                )
            )
        }
    }

}