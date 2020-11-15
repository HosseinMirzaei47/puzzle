package com.example.puzzleapp.ui

import android.view.View

interface AdapterCallbacks {
    fun onPieceClicked(position: Int, id: Int, view: View)
}