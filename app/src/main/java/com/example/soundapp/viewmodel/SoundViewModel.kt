package com.example.soundapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soundapp.repository.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(
    private val soundRepository: SoundRepository
) : ViewModel() {
    private val _soundName = MutableLiveData<List<String>>()
    val soundName: LiveData<List<String>> = _soundName

    fun loadSounds(context: Context) {
        _soundName.value = soundRepository.getSoundNames(context)
    }
}