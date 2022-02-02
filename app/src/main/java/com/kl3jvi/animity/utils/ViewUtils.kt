package com.kl3jvi.animity.utils

import android.view.View

object ViewUtils {

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

}