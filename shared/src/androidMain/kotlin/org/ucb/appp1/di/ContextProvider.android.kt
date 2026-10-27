package org.ucb.appp1.di

import android.content.Context

actual class ContextProvider(private val context: Context) {
    actual fun getContext(): Any = context
}