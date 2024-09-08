package com.example.soundapp.adapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundapp.R
import com.example.soundapp.database.AppDatabase
import com.example.soundapp.databinding.ItemGroupHeaderBinding
import com.example.soundapp.databinding.ItemSoundControlBinding
import com.example.soundapp.entity.Sound
import com.example.soundapp.model.SoundGroupWithSounds
import com.example.soundapp.utils.NOTIFICATION_ID
import com.example.soundapp.utils.SoundPlayer
import com.example.soundapp.utils.showCustomNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayingSoundsAdapter(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val soundGroups: ArrayList<SoundGroupWithSounds>,
    private val soundPlayer: SoundPlayer
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var database: AppDatabase
    private lateinit var binding: ItemSoundControlBinding
    private val VIEW_TYPE_GROUP = 0
    private val VIEW_TYPE_SOUND = 1
    private val expandedGroups = mutableSetOf<Int>()
    private val playPauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshPlayPauseIcons()
        }
    }

    init {
        val filter = IntentFilter("com.example.soundapp.ACTION_PLAY_PAUSE")
        LocalBroadcastManager.getInstance(context).registerReceiver(this.playPauseReceiver, filter)
    }


    private fun refreshPlayPauseIcons() {
        soundGroups.forEachIndexed { index, group ->
            notifyItemChanged(index)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGroupHeader(position)) VIEW_TYPE_GROUP else VIEW_TYPE_SOUND
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        database = AppDatabase.getDatabase(parent.context)
        return if (viewType == VIEW_TYPE_GROUP) {
            val binding =
                ItemGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GroupViewHolder(binding)
        } else {
            val binding =
                ItemSoundControlBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SoundViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GroupViewHolder) {
            holder.bind(soundGroups[getGroupPosition(position)], position)
        } else if (holder is SoundViewHolder) {
            val soundPosition = getSoundPosition(position)
            holder.bind(
                soundPosition,
                soundPlayer
            )
        }
    }

    override fun getItemCount(): Int {
        var count = soundGroups.size
        for (group in soundGroups) {
            if (expandedGroups.contains(soundGroups.indexOf(group))) {
                count += group.sounds.size
            }
        }
        return count
    }

    fun removeItem(context: Context, position: Pair<Int, Int>, sound: Sound) {
        try {
            val groupPosition = position.first
            val soundPosition = position.second
            if (position.first < soundGroups.size) {
                soundPlayer.clearSound("${soundGroups[groupPosition].groupName}_${sound.name}")
                soundGroups[groupPosition].sounds.removeAt(soundPosition)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        database.soundDao().deleteSound(sound.id)
                        if (soundGroups[groupPosition].sounds.isEmpty()) {
                            database.soundGroupDao()
                                .deleteSoundGroupByName(soundGroups[groupPosition].groupName)
                            expandedGroups.remove(groupPosition)
                            soundGroups.removeAt(groupPosition)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (soundGroups.isEmpty()) {
                            val notificationManager = NotificationManagerCompat.from(context)
                            notificationManager.cancel(NOTIFICATION_ID)
                            SoundPlayer.stopAllSounds()
                        }
                        notifyDataSetChanged()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isGroupHeader(position: Int): Boolean {
        var index = 0
        for (i in soundGroups.indices) {
            if (position == index) return true
            index += if (expandedGroups.contains(i)) soundGroups[i].sounds.size + 1 else 1
        }
        return false
    }

    private fun getGroupPosition(position: Int): Int {
        var index = 0
        try {
            for (i in soundGroups.indices) {
                if (position == index) return i
                index += if (expandedGroups.contains(i)) soundGroups[i].sounds.size + 1 else 1
            }
        } catch (e: Exception) {
            Toast.makeText(
                binding.root.context,
                "Something went wrong, please try again later...",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        return -1
    }

    private fun getSoundPosition(position: Int): Pair<Int, Int> {
        var index = 0
        try {
            for (i in soundGroups.indices) {
                if (position == index) return 0 to 0
                index++
                if (expandedGroups.contains(i)) {
                    for (j in soundGroups[i].sounds.indices) {
                        if (position == index) return i to j
                        index++
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                binding.root.context,
                "Something went wrong, please try again later...",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        return -1 to -1
    }

    inner class GroupViewHolder(private val binding: ItemGroupHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: SoundGroupWithSounds, position: Int) {
            val groupPosition = getGroupPosition(position)
            SoundPlayer.setLastGroupId(groupPosition)
            binding.tvGroupName.text = group.groupName
            try {
                binding.ivExpandCollapse.setImageResource(
                    if (expandedGroups.contains(groupPosition)) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            binding.ivExpandCollapse.setOnClickListener {
                if (expandedGroups.contains(groupPosition)) {
                    expandedGroups.remove(groupPosition)
                    notifyItemRangeRemoved(position + 1, group.sounds.size)
                } else {
                    expandedGroups.add(groupPosition)
                    notifyItemRangeInserted(position + 1, group.sounds.size)
                }
                notifyItemChanged(position)
            }

            // Check if the current group is playing and update the button
            val isPlaying =
                soundGroups[groupPosition].sounds.any { soundPlayer.isPlaying("${soundGroups[groupPosition].groupName}_${it.name}") }
            binding.btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.btnPlayPause.setOnClickListener {
                val isGroupPlaying = soundPlayer.isGroupPlaying(
                    soundGroups[groupPosition].groupName,
                    soundGroups[groupPosition].sounds
                )
                if (!isGroupPlaying) {
                    // Pause other sounds
                    soundPlayer.pauseSound()

                    // Play the selected group
                    soundPlayer.playGroups(
                        binding.root.context,
                        soundGroups[groupPosition].sounds,
                        soundGroups[groupPosition].groupName
                    )
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
                    sendPlayPauseBroadcast(
                        soundGroups[groupPosition].sounds,
                        soundGroups[groupPosition].groupName,
                        true
                    )
                } else {
                    soundPlayer.pauseSound()
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                    sendPlayPauseBroadcast(
                        soundGroups[groupPosition].sounds,
                        soundGroups[groupPosition].groupName,
                        false
                    )
                    showCustomNotification(
                        context,
                        soundGroups[groupPosition].sounds,
                        soundGroups[groupPosition].groupName,
                        false
                    )
                }
                notifyItemChanged(groupPosition)
            }

            binding.btnDelete.setOnClickListener {
                soundGroups[groupPosition].sounds.forEach { sound ->
                    SoundPlayer.clearSound("${soundGroups[groupPosition].groupName}_${sound.name}")
                }
                SoundPlayer.clearSelectedList()
                expandedGroups.remove(groupPosition)
                soundGroups.removeAt(groupPosition)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        database.soundGroupDao().deleteSoundGroupByName(group.groupName)
                    }
                    withContext(Dispatchers.Main) {
                        if (soundGroups.isEmpty()) {
                            val notificationManager = NotificationManagerCompat.from(context)
                            notificationManager.cancel(NOTIFICATION_ID)
                        }
                        SoundPlayer.stopAllSounds()
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun sendPlayPauseBroadcast(
        soundList: ArrayList<Sound>,
        soundName: String,
        isPlaying: Boolean
    ) {
        val intent = Intent("com.example.soundapp.ACTION_PLAY_PAUSE").apply {
            putParcelableArrayListExtra("SOUND_LIST", soundList)
            putExtra("SOUND_NAME", soundName)
            putExtra("IS_PLAYING", isPlaying)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    inner class SoundViewHolder(private val binding: ItemSoundControlBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            soundPosition: Pair<Int, Int>,
            soundPlayer: SoundPlayer
        ) {
            val sound = soundGroups[soundPosition.first].sounds[soundPosition.second]
            binding.tvSoundName.text = sound.name
            binding.seekBarVolume.progress = sound.progress
            binding.seekBarVolume.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    soundPlayer.setVolume(
                        "${soundGroups[soundPosition.first].groupName}_${sound.name}",
                        progress
                    )
                    SoundPlayer.updateSoundVolume(progress, sound.name)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            database.soundDao()
                                .updateProgress(sound.name, sound.groupId, seekBar?.progress ?: 100)
                        }
                    }
                }
            })
            binding.btnDelete.setOnClickListener {
                removeItem(binding.root.context, soundPosition, sound)
            }
        }
    }
}
