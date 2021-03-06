package com.example.puzzleapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.R
import com.example.puzzleapp.databinding.FragmentLevelBinding
import com.example.puzzleapp.utils.Levels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LevelFragment : Fragment(R.layout.fragment_level) {

    private lateinit var binding: FragmentLevelBinding

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
        onDieHardLevelClick()
    }

    private fun navigateToGameFragment(difficulty: Int) {
        when (binding.radioGroup.checkedRadioButtonId) {
            R.id.jigsawMode -> {
                findNavController().navigate(
                    LevelFragmentDirections.actionLevelFragmentToJigsawFragment(
                        difficulty
                    )
                )
            }
            R.id.swipeMode -> {
                findNavController().navigate(
                    LevelFragmentDirections.actionLevelFragmentToNewPuzzleFragment(
                        difficulty,
                        MODE_SWIPE
                    )
                )
            }
            R.id.dragMode -> {
                findNavController().navigate(
                    LevelFragmentDirections.actionLevelFragmentToNewPuzzleFragment(
                        difficulty,
                        MODE_DRAG
                    )
                )
            }
            R.id.clickMode -> {
                findNavController().navigate(
                    LevelFragmentDirections.actionLevelFragmentToPuzzleGameFragment(
                        difficulty,
                        MODE_SWIPE
                    )
                )
            }
            else -> {
                findNavController().navigate(
                    LevelFragmentDirections.actionLevelFragmentToPuzzleGameFragment(
                        difficulty,
                        MODE_SWIPE
                    )
                )
            }
        }
    }

    private fun onHardLevelClick() {
        binding.levelHardButton.setOnClickListener {
            navigateToGameFragment(Levels.LEVEL_HARD)
        }
    }

    private fun onDieHardLevelClick() {
        binding.levelDieHardButton.setOnClickListener {
            navigateToGameFragment(Levels.LEVEL_DIE_HARD)
        }
    }

    private fun onNormalLevelClick() {
        binding.levelNormalButton.setOnClickListener {
            navigateToGameFragment(Levels.LEVEL_NORMAL)
        }
    }

    private fun onEasyLevelClick() {
        binding.levelEasyButton.setOnClickListener {
            navigateToGameFragment(Levels.LEVEL_EASY)
        }
    }

    companion object {
        const val MODE_SWIPE = 0
        const val MODE_DRAG = 1
    }

}