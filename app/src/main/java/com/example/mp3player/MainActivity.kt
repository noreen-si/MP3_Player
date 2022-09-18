package com.example.mp3player

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.example.mp3player.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listView: ListView
    private lateinit var allItems: Array<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        listView = binding.ListViewAllSongs

        // This will request permission from the user to access media on their device.
        runtimePermission()

    }

    // Request permission
    private fun runtimePermission() {
        // Use Dexter to simplify the process of requesting permission
        // This MP3 app will need permission to read the device's storage.
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    // When permission is granted by the user, the songs will appear in the list view.
                    showSongs()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    fun findSong(file : File) : ArrayList<File> {

        val allSongs = ArrayList<File>()
        val allFiles = file.listFiles()

        if (allFiles != null) {
            for(individualFile : File in allFiles) {
                if (individualFile.isDirectory && !individualFile.isHidden) {
                    // Add the songs from a directory within the device
                    allSongs.addAll(findSong(individualFile))
                }
                else {
                    // Add the songs that are in a .mp3 format
                    if(individualFile.name.endsWith(".mp3")) {
                        allSongs.add(individualFile)
                    }
                }
            }
        }
        return allSongs
    }

    fun showSongs() {
        // Finds all the songs (.mp3 format)
        val mySongs = findSong(Environment.getExternalStorageDirectory())

        // This will get rid of the ".mp3" ending to the song files.
       allItems = arrayOfNulls(mySongs.size)
        for (i in allItems.indices) {
            allItems[i] = mySongs[i].name.replace(".mp3", "")
        }

        // Set the data for the listView
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allItems)
        listView.adapter = adapter

        // Brings user to the song screen once they click an item from the list of songs
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val songName = listView.getItemAtPosition(position).toString()

                val intent = Intent(this, SongActivity::class.java)
                    .putExtra("song name", songName)
                    .putExtra("all songs", mySongs)
                    .putExtra("position", position)
                startActivity(intent)
            }
    }
}
