package com.example.puzzleapp

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class Tile(
    val id: Int,
    val bitmap: Bitmap
)