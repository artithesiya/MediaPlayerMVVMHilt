package com.example.soundapp.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundapp.adapter.PlayingSoundsAdapter
import com.example.soundapp.adapter.SoundAdapter
import com.example.soundapp.database.AppDatabase
import com.example.soundapp.databinding.ActivityMainBinding
import com.example.soundapp.databinding.BottomSheetLayoutBinding
import com.example.soundapp.databinding.CustomToastBinding
import com.example.soundapp.entity.Sound
import com.example.soundapp.entity.SoundGroup
import com.example.soundapp.model.SoundGroupWithSounds
import com.example.soundapp.repository.SoundRepository
import com.example.soundapp.utils.SoundPlayer
import com.example.soundapp.utils.createNotificationChannel
import com.example.soundapp.viewmodel.SoundViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SoundViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*For Splash Screen*/
        installSplashScreen()
        /*Layout Binding*/
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
        createNotificationChannel(this)
        setupRecyclerview()
        observeList()
        floatingButtonClick()
    }

    private fun floatingButtonClick() {
        binding.floatingActionButton.setOnClickListener {
            lifecycleScope.launch {
                var soundGroups = getSoundGroupsFromDatabase()
                soundGroups = getSoundGroupsFromList(soundGroups)
                if (soundGroups.isNotEmpty()) {
                    showPlayingSoundsBottomSheet(soundGroups)
                } else {
                    Toast.makeText(this@MainActivity, "No sounds found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerview() {
        /*set Layout for recyclerview*/
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
        }
    }

    private fun observeList() {
        /*Observe LiveData*/
        viewModel.soundName.observe(this) { soundNames ->
            sortList(soundNames)
            binding.recyclerView.adapter = SoundAdapter(soundNames) { sound ->
                soundItemClick(sound)
            }
        }
        viewModel.loadSounds(this@MainActivity)
    }

    private fun initialization() {
        /*Initialize ViewModel and Repository*/
        val soundRepository = SoundRepository()
        viewModel = SoundViewModel(soundRepository)
        database = AppDatabase.getDatabase(this)
    }

    private fun sortList(soundNames: List<String>) {
        // Sort the list based on the numeric part of the strings
        Collections.sort(soundNames) { o1, o2 ->
            val pattern = Regex("\\d+")
            val num1 = pattern.find(o1)?.value?.toIntOrNull() ?: 0
            val num2 = pattern.find(o2)?.value?.toIntOrNull() ?: 0
            num1.compareTo(num2)
        }
    }

    private fun soundItemClick(sound: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
        val selectedList = ArrayList<Sound>()
        selectedList.add(Sound(name = sound, groupId = 0, progress = 100))
        SoundPlayer.setSelectedList(selectedList)
        SoundPlayer.playGroups(this@MainActivity, selectedList, "New Group")
        showCustomDialog(this@MainActivity, "New Group", sound)
    }

    private fun showCustomDialog(context: Context, groupName: String, soundName: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val binding = CustomToastBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        binding.toastSoundName.text = soundName
        val initialVolume = SoundPlayer.getVolume()
        binding.toastSeekBar.progress = initialVolume
        binding.toastSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SoundPlayer.setVolume("${groupName}_${soundName}", progress)
                SoundPlayer.updateSoundVolume(progress, soundName)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        database.soundDao()
                            .updateProgress(soundName, 0, seekBar?.progress ?: 100)
                    }
                }
            }
        })
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            dialog.dismiss()
        }, 2000)
    }


    private suspend fun getSoundGroupsFromDatabase(): ArrayList<SoundGroupWithSounds> {
        val soundList: ArrayList<SoundGroupWithSounds> = ArrayList()
        val soundGroups = database.soundGroupDao().getAllSoundGroups()
        soundGroups.map { soundGroup ->
            val sounds = database.soundDao().getSoundsByGroupId(soundGroup.id)
            soundList.add(
                SoundGroupWithSounds(
                    groupName = soundGroup.name,
                    sounds = sounds.toCollection(ArrayList())
                )
            )
        }
        return soundList
    }

    private fun getSoundGroupsFromList(soundGroups: ArrayList<SoundGroupWithSounds>): ArrayList<SoundGroupWithSounds> {
        if (SoundPlayer.getSelectedList().size > 0) {
            soundGroups.add(
                SoundGroupWithSounds(
                    groupName = "New Group",
                    sounds = SoundPlayer.getSelectedList()
                )
            )
            return soundGroups
        } else {
            return soundGroups
        }
    }

    private fun showPlayingSoundsBottomSheet(soundGroups: ArrayList<SoundGroupWithSounds>) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetBinding.rvPlayingSounds.adapter = PlayingSoundsAdapter(
            this@MainActivity,
            lifecycleScope,
            soundGroups,
            SoundPlayer
        )
        bottomSheetBinding.btnSave.setOnClickListener {
            saveSoundWithDialog(bottomSheetDialog)

        }
        bottomSheetDialog.show()
    }

    private fun saveSoundWithDialog(bottomSheetDialog: BottomSheetDialog) {
        if (SoundPlayer.getSelectedList()
                .isNotEmpty() && SoundPlayer.getSelectedList().size > 0
        ) {
            val dialogLayout: AlertDialog = AlertDialog.Builder(this@MainActivity).apply {
                setTitle("Save")
                setMessage("New Group will be added")
                setPositiveButton("OK") { _, _ -> }
                setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            }.create()
            dialogLayout.setOnShowListener {
                val positiveButton = dialogLayout.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    saveSound(bottomSheetDialog, dialogLayout)

                }
            }

            dialogLayout.show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "New group not found.. , to add sound tap on sound box",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveSound(bottomSheetDialog: BottomSheetDialog, dialogLayout: AlertDialog) {
        lifecycleScope.launch {
            val soundGroup =
                SoundGroup(name = "Group " + (SoundPlayer.getLastGroupId() + 1))
            withContext(Dispatchers.IO) {
                val insertedGroupId =
                    database.soundGroupDao().insertSoundGroup(soundGroup)
                val sounds: ArrayList<Sound> = ArrayList()
                SoundPlayer.getSelectedList().forEach { sound ->
                    sounds.add(
                        Sound(
                            name = sound.name,
                            groupId = insertedGroupId.toInt(),
                            progress = sound.progress
                        )
                    )
                }
                sounds.forEach {
                    database.soundDao().insertSound(it)
                }
            }
            withContext(Dispatchers.Main) {
                SoundPlayer.getSelectedList().clear()
                SoundPlayer.stopAllSounds()
                bottomSheetDialog.dismiss()
                dialogLayout.dismiss()
            }
        }
    }
}

