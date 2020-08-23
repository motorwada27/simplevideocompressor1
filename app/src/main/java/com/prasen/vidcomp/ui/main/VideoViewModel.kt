package com.prasen.vidcomp.ui.main

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prasen.vidcomp.utils.FFMpegUtils

class VideoViewModel : ViewModel(),CompressionListener {

    private val _videoPlaybackUri = MutableLiveData<Uri>()

    val playbackUri:MutableLiveData<Uri>
    get() = _videoPlaybackUri

    fun initializeFFMpeg(context: Context){
        FFMpegUtils.initializeFFMpeg(context)
    }

    fun compressVideoFile(inputPath: String?,bitrate: String){
        FFMpegUtils.compress(inputPath,bitrate,this)
    }

    override fun compressionFinished(status: Int, isVideo: Boolean, fileOutputPath: String?) {
        Log.d(TAG,"compressionFinished")
        _videoPlaybackUri.postValue(Uri.parse(fileOutputPath))
    }

    override fun onFailure(message: String?) {
        Log.d(TAG,"onFailure")
    }

    override fun onProgress(progress: Int) {
        Log.d(TAG,"onProgress")
    }

    companion object{
        val TAG = VideoViewModel::class.java.simpleName
    }
}
