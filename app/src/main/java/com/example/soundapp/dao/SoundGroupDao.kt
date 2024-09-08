package com.example.soundapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.soundapp.entity.SoundGroup

@Dao
interface SoundGroupDao {
    @Insert
    suspend fun insertSoundGroup(soundGroup: SoundGroup): Long

    @Query("SELECT * FROM sound_groups")
    suspend fun getAllSoundGroups(): List<SoundGroup>

    @Query("SELECT * FROM sound_groups WHERE id = :groupId")
    suspend fun getSoundGroupById(groupId: Int): SoundGroup

    @Query("DELETE FROM sound_groups WHERE name = :groupName")
    suspend fun deleteSoundGroupByName(groupName: String)
}
