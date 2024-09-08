package com.example.soundapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.soundapp.entity.Sound

@Dao
interface SoundDao {
    @Insert
    suspend fun insertSound(sound: Sound)

    @Query("SELECT * FROM sounds WHERE groupId = :groupId")
    suspend fun getSoundsByGroupId(groupId: Int): List<Sound>

    @Query("SELECT id FROM sounds WHERE name= :soundName AND groupId = :groupId")
    suspend fun getSoundIdByName(soundName: String, groupId: Int): Int

    @Query("DELETE FROM sounds WHERE id = :soundId")
    suspend fun deleteSound(soundId: Int)


    @Query("UPDATE sounds SET progress = :progress WHERE name = :soundName AND groupId = :groupId")
    suspend fun updateProgress(soundName: String, groupId: Int, progress: Int)

}
