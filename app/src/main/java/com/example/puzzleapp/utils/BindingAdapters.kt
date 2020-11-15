package com.example.puzzleapp.utils

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("android:loadImage")
fun loadPicture(view: ImageView, url: Bitmap?) {
    url?.let {
        Glide.with(view).load(it).into(view)
    }
}

@BindingAdapter("android:loadImage")
fun loadPicture(view: ImageView, url: Int) {
    Glide.with(view).load(url).into(view)
}