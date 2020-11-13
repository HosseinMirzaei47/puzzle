package com.example.puzzleapp

import android.view.View

interface AdapterCallbacks {
    fun onPieceClicked(position: Int, id: Int, view: View)
}