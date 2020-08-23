package com.prasen.vidcomp.ui.main

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.prasen.vidcomp.R
import com.prasen.vidcomp.databinding.MainFragmentBinding


class VideoFragment (path: Uri): Fragment(){

    var video: VideoView? = null
    var mBitrate : EditText? = null
    var currentVideo : Uri = path
    var submitButton : Button? = null
    var playPauseButton : Button? = null
    private lateinit var viewModel: VideoViewModel
    private lateinit var binding:MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG,"onCreateView $currentVideo")
        binding = DataBindingUtil.inflate(inflater,R.layout.main_fragment, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VideoViewModel::class.java)
        activity?.let { viewModel.initializeFFMpeg(it.applicationContext) }
        viewModel.playbackUri.observe(this, Observer {
                uri -> startVideoPlayback(uri,isPlayPauseEnabled = true)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonText = "Submit"
        binding.playPauseButtontext = "Pause"
        video = view.findViewById(R.id.videoview) as VideoView
        mBitrate = view.findViewById(R.id.bitrate_text)
        submitButton = view.findViewById(R.id.submit_button)
        submitButton?.setOnClickListener {
            val path = getPath(currentVideo)
            val result = mBitrate?.text?.toString() + "k"
            Log.d(TAG,"submitted bitrate is ${result}" )
            viewModel.compressVideoFile(path,result)
        }
        playPauseButton = view.findViewById(R.id.play_pause_button)
        playPauseButton?.setOnClickListener {
          video?.let {
              if(it.isPlaying){
                  it.pause()
                  binding.playPauseButtontext = "Play"
              }else{
                  it.resume()
                  binding.playPauseButtontext = "Pause"
              }
          }
        }
        Log.d(TAG,"onViewCreated $currentVideo")
        startVideoPlayback(currentVideo,isPlayPauseEnabled = false)
    }

    fun startVideoPlayback(uri : Uri,isPlayPauseEnabled:Boolean){
        Log.d(TAG,"startVideoPlayback $uri")
        playPauseButton?.visibility = if(isPlayPauseEnabled) View.VISIBLE else View.GONE
        video?.setVideoURI(uri)
        video?.start()
        video?.setOnPreparedListener {
            //close the progress dialog when buffering is done
            Log.d(TAG,"video is starting with path $uri")
        }
    }

    fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = activity?.contentResolver?.query(uri!!, projection, null, null, null)
        return if (cursor != null) { // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    companion object {

        val TAG = VideoFragment::class.java.simpleName

        fun startVideo(path: Uri) : Fragment{
            return VideoFragment(path)
        }
    }

}
