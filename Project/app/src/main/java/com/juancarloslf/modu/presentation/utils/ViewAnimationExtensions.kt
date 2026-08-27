package com.juancarloslf.modu.presentation.utils

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible

const val ROTATION_COLLAPSED = 0f
const val ROTATION_EXPANDED = 180f
const val ACCORDION_ANIM_DURATION = 300L

fun View.setupAccordion(icon: ImageView, vararg contentViews: View) {
    this.setOnClickListener {
        val isCurrentlyVisible = contentViews.firstOrNull()?.isVisible == true

        contentViews.forEach { view ->
            view.isVisible = !isCurrentlyVisible
        }

        val rotationAngle = if (isCurrentlyVisible) ROTATION_EXPANDED else ROTATION_COLLAPSED
        icon.animate().rotation(rotationAngle).setDuration(ACCORDION_ANIM_DURATION).start()
    }
}