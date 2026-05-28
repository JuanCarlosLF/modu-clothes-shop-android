package com.example.modu.presentation.detail

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.modu.R

fun mapColor(context: Context, color: String): Int {
    return when (color.uppercase()) {
        "BLACK" -> ContextCompat.getColor(context, R.color.black_background)
        "BLUE" -> ContextCompat.getColor(context, R.color.aquamarine_tertiary)
        "GREEN" -> ContextCompat.getColor(context, R.color.green)
        "RED" -> ContextCompat.getColor(context, R.color.red)
        else -> ContextCompat.getColor(context, R.color.white)
    }
}