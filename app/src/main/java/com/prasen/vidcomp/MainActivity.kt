package com.prasen.vidcomp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.prasen.vidcomp.ui.main.VideoFragment
import com.prasen.vidcomp.utils.FFMpegUtils


class MainActivity : AppCompatActivity() {

    private lateinit var  launchGalleryButton : Button
    private val REQUEST_TAKE_GALLERY_VIDEO = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        launchGalleryButton = findViewById(R.id.button)
        launchGalleryButton.setOnClickListener {
            launchGallery()
            launchGalleryButton.visibility = View.GONE // Remove this workaround later
        }
    }

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            111
        )
    }

    private fun launchGallery(){
        val intent = Intent(
            Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.type = "video/*"
        startActivityForResult(
           intent, REQUEST_TAKE_GALLERY_VIDEO
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val selectedImageUri: Uri? = data?.data

                if (selectedImageUri != null) {
                    Log.d(TAG,"selected file path is $selectedImageUri")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, VideoFragment.startVideo(selectedImageUri))
                        .commitNow()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"permission granted WRITE_EXTERNAL_STORAGE")
        }
    }

    private fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) { // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }


    companion object {

        val TAG = MainActivity::class.java.simpleName
    }
}
