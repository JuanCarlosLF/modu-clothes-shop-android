package com.example.modu.presentation.utils

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible

const val ROTATION_COLLAPSED = 0f
const val ROTATION_EXPANDED = 180f
const val ACCORDION_ANIM_DURATION = 300L

fun View.setupAccordion(arrowIcon: ImageView, vararg contentViews: View) {
    this.setOnClickListener {
        val isCurrentlyVisible = contentViews.firstOrNull()?.isVisible == true
        val newVisibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE

        contentViews.forEach { it.visibility = newVisibility }

        val rotationAngle = if (isCurrentlyVisible) ROTATION_EXPANDED else ROTATION_COLLAPSED
        arrowIcon.animate().rotation(rotationAngle).setDuration(ACCORDION_ANIM_DURATION).start()
    }
}