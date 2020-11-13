package com.example.puzzleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.databinding.FragmentPuzzlesBinding

class PuzzlesFragment : Fragment() {

    private lateinit var binding: FragmentPuzzlesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPuzzlesBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val puzzles = listOf(R.drawable.scarlett_johansson, R.drawable.puzzle_image)
        showPuzzles(puzzles)
    }

    private fun showPuzzles(puzzles: List<Int>) {
        binding.recyclerPuzzles.withModels {
            puzzles.forEachIndexed { index, imageSource ->
                itemPuzzle {
                    id(index)
                    imageSource(imageSource)
                    onPuzzleClick { _ ->
                        findNavController().navigate(
                            PuzzlesFragmentDirections.actionPuzzlesFragment2ToLevelFragment(
                                imageSource
                            )
                        )
                    }
                }
            }
        }
    }

}