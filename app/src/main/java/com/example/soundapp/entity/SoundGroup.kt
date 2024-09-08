package com.example.soundapp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sound_groups")
data class SoundGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
