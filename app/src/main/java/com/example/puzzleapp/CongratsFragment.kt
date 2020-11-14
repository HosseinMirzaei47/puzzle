package com.example.puzzleapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.databinding.FragmentCongratsBinding

class CongratsFragment : Fragment() {

    private lateinit var binding: FragmentCongratsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(CongratsFragmentDirections.actionCongratsFragmentToPuzzlesFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCongratsBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            username = getString(R.string.username) + " Hossein Mirzaei"
            gameDuration = "Game duration: 01:26"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showConfetti()
        setOnClicks()
    }

    private fun setOnClicks() {
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

    private fun showConfetti() {
        binding.congratsConfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(2f, 10f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .setPosition(+60f, binding.congratsConfetti.width + 50f, -50f, 100f)
            .streamFor(300, 3000L)
    }

}