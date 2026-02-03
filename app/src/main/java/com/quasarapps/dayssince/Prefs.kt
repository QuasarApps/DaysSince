package com.quasarapps.dayssince

import android.content.Context
import android.content.SharedPreferences

/**
 * Minimal persistent key-value storage.
 *
 * This uses MODE_PRIVATE SharedPreferences. This is already private to your app sandbox.
 * If you want encryption at-rest later, we can swap this to AndroidX Security Crypto.
 */
object Prefs {
    private const val FILE_NAME = "dayssince_prefs"

    fun get(context: Context): SharedPreferences {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }
}

