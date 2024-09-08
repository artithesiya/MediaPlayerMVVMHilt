package com.example.soundapp.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.soundapp.entity.Sound

object SoundPlayer {
    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()
    private var lastGroupId: Int = 0
    private var selectedList: ArrayList<Sound> = ArrayList()
    private var currentlyPlayingGroup: String? = null

    fun playGroups(context: Context, soundList: ArrayList<Sound>, groupName: String) {
        soundList.forEach { sound ->
            val uniqueKey = "${groupName}_${sound.name}"
            val soundResId = context.resources.getIdentifier(sound.name, "raw", context.packageName)
            val mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer.start()
            mediaPlayer.isLooping = true
            val volumeLevel = sound.progress / 100f
            mediaPlayer.setVolume(volumeLevel, volumeLevel)
            mediaPlayers[uniqueKey] = mediaPlayer

        }
        showCustomNotification(context, soundList, groupName, true)
    }

    fun isGroupPlaying(groupName: String, soundList: List<Sound>): Boolean {
        return soundList.any { sound ->
            val uniqueKey = "${groupName}_${sound.name}"
            mediaPlayers[uniqueKey]?.isPlaying == true
        }
    }

    fun pauseSound() {
        mediaPlayers.forEach { (_, mediaPlayer) ->
            mediaPlayer.pause()
            mediaPlayer.release()
        }
        mediaPlayers.clear()
        currentlyPlayingGroup = null

    }


    fun stopAllSounds() {
        for ((_, mediaPlayer) in mediaPlayers) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayers.clear()
    }

    fun getVolume(): Int {
        return 100
    }

    fun setVolume(uniqueId: String, volume: Int) {
        val mediaPlayer = mediaPlayers[uniqueId] ?: return
        val volumeLevel = volume / 100f
        mediaPlayer.setVolume(volumeLevel, volumeLevel)
    }

    fun isPlaying(uniqueId: String): Boolean {
        return mediaPlayers[uniqueId]?.isPlaying ?: false
    }

    fun isPlaying(): Boolean {
        return mediaPlayers.values.any { it.isPlaying }
    }

    fun clearSound(uniqueId: String) {
        mediaPlayers[uniqueId]?.stop()
        mediaPlayers[uniqueId]?.release()
        mediaPlayers.remove(uniqueId)
    }

    fun setSelectedList(list: ArrayList<Sound>) {
        selectedList.addAll(list)
    }

    fun updateSoundVolume(volume: Int, soundName: String) {
        selectedList.forEach {
            if (it.name == soundName) {
                it.progress = volume
            }
        }

    }

    fun clearSelectedList() {
        selectedList.clear()
    }

    fun getSelectedList(): ArrayList<Sound> {
        return selectedList
    }

    fun getLastGroupId(): Int {
        return lastGroupId
    }

    fun setLastGroupId(groupId: Int) {
        lastGroupId = groupId
    }
}
