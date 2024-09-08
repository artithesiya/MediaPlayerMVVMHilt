package com.example.soundapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundapp.databinding.ItemSoundBinding

class SoundAdapter(
    private val sounds: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    class SoundViewHolder(
        private val binding: ItemSoundBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentSound: String? = null

        init {
            binding.root.setOnClickListener {
                currentSound?.let { onClick(it) }
            }
        }

        fun bind(sound: String) {
            currentSound = sound
            binding.soundNameTextView.text = sound
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoundViewHolder(binding, onClick)
    }

    override fun getItemCount(): Int {
        return sounds.size
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds[position]
        holder.bind(sound)
    }
}