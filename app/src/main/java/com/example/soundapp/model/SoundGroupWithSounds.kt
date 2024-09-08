package com.example.soundapp.model

import com.example.soundapp.entity.Sound

data class SoundGroupWithSounds(
    val groupName: String,
    val sounds: ArrayList<Sound>
)
