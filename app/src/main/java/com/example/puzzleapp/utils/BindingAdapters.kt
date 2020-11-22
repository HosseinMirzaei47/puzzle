package com.example.puzzleapp.utils

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("android:loadImage")
fun loadPicture(view: ImageView, url: Bitmap?) {
    Glide.with(view).load(url).into(view).clearOnDetach()
}

@BindingAdapter("android:circleImageSrc")
fun circleImageSrc(view: ImageView, url: Int) {
    Glide.with(view).load(url).circleCrop().into(view).clearOnDetach()
}

@BindingAdapter("android:visibleOnResult")
fun visibleOnResult(view: View, flag: Boolean) {
    view.isVisible = flag
}