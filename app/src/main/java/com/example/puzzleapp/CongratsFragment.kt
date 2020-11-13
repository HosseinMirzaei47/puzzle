package com.example.puzzleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.databinding.FragmentCongratsBinding

class CongratsFragment : Fragment() {

    private lateinit var binding: FragmentCongratsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCongratsBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            username = getString(R.string.username) + "Hossein Mirzaei"
            gameDuration = "Game duration: 01:26"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.congratsPlayAgain.setOnClickListener {
            findNavController().navigate(
                CongratsFragmentDirections.actionCongratsFragmentToLevelFragment(
                    R.drawable.puzzle_image
                )
            )
        }

        binding.congratsHome.setOnClickListener {
            findNavController().navigate(CongratsFragmentDirections.actionCongratsFragmentToPuzzlesFragment())
        }

    }

}