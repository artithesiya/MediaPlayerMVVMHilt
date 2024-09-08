package com.example.soundapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.soundapp.dao.SoundDao
import com.example.soundapp.dao.SoundGroupDao
import com.example.soundapp.entity.Sound
import com.example.soundapp.entity.SoundGroup

@Database(entities = [SoundGroup::class, Sound::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundGroupDao(): SoundGroupDao
    abstract fun soundDao(): SoundDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sound_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
