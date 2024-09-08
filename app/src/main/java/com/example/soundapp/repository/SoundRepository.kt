package com.example.soundapp.repository

import android.content.Context
import com.example.soundapp.R
import javax.inject.Inject

class SoundRepository @Inject constructor() {

    fun getSoundNames(context: Context): List<String> {
        val soundRes = getRawResourceIds()
        return soundRes.map {
            context.resources.getResourceEntryName(it)
        }
    }

    private fun getRawResourceIds(): List<Int> {
        val rawResourceClass = R.raw::class.java
        return rawResourceClass.fields.map { it.getInt(null) }
    }
}