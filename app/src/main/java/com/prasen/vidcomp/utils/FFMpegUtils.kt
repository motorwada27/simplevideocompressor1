package com.prasen.vidcomp.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.prasen.vidcomp.ui.main.CompressionListener
import java.io.File


object FFMpegUtils {

    val TAG = FFMpegUtils::class.java.simpleName
    lateinit var FFmpegInstance : FFmpeg

    fun initializeFFMpeg(context: Context) {
        FFmpegInstance = FFmpeg.getInstance(context);
        try {
            FFmpegInstance.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onSuccess() {
                    super.onSuccess()
                    Toast.makeText(context, "FFMPEG is supported by the device", Toast.LENGTH_SHORT)
                        .show()
                }
            });
        } catch (e: FFmpegNotSupportedException) {
            // Handle if FFmpeg is not supported by device
            Toast.makeText(context, "FFMPEG is not supported", Toast.LENGTH_SHORT).show()
        }
    }

    fun compress(inputPath: String?, bitrate:String, listener: CompressionListener?) {
        if (inputPath == null || inputPath.isEmpty()) {
            if (listener != null) {
                listener.compressionFinished(
                    0,
                    false,
                    null
                )
            }
            return
        }
        var outputPath = ""
        outputPath = getAppDir() + "/video_compress.mp4"
        val commandParams = arrayOfNulls<String>(26)
        commandParams[0] = "-y"
        commandParams[1] = "-i"
        commandParams[2] = inputPath
        commandParams[3] = "-s"
        commandParams[4] = "720x480"
        commandParams[5] = "-r"
        commandParams[6] = "20"
        commandParams[7] = "-c:v"
        commandParams[8] = "libx264"
        commandParams[9] = "-preset"
        commandParams[10] = "ultrafast"
        commandParams[11] = "-c:a"
        commandParams[12] = "copy"
        commandParams[13] = "-me_method"
        commandParams[14] = "zero"
        commandParams[15] = "-tune"
        commandParams[16] = "fastdecode"
        commandParams[17] = "-tune"
        commandParams[18] = "zerolatency"
        commandParams[19] = "-strict"
        commandParams[20] = "-2"
        commandParams[21] = "-b:v"
        commandParams[22] = bitrate
        commandParams[23] = "-pix_fmt"
        commandParams[24] = "yuv420p"
        commandParams[25] = outputPath
        compressVideo(commandParams, outputPath, listener)
    }

    fun getAppDir(): String? {
        var outputPath =
            Environment.getExternalStorageDirectory().absolutePath
        outputPath += "/" + "com.prasen.vidcomp"
        var file = File(outputPath)
        if (!file.exists()) {
            file.mkdir()
        }
        outputPath += "/" + "CompressedVideos"
        file = File(outputPath)
        if (!file.exists()) {
            file.mkdir()
        }
        return outputPath
    }

    private fun compressVideo(command: Array<String?>, outputFilePath: String, listener: CompressionListener?) {
        var status = 0
        try {
            FFmpegInstance.execute(command, object : FFmpegExecuteResponseHandler {
                override fun onSuccess(message: String) {
                    status = 2
                    Log.d(TAG, "onSuccess $message ")
                }

                override fun onProgress(message: String) {
                    status = 3
                    Log.d(TAG, "onProgress $message ")
                }

                override fun onFailure(message: String) {
                    status = 4
                    Log.d(TAG, "failure $message ")
                    listener?.onFailure("Error : $message")
                }

                override fun onStart() {}

                override fun onFinish() {
                    Log.d(TAG, "finished ")
                    listener?.compressionFinished(status, true, outputFilePath)
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            status = 4
            Log.d(TAG, "failure exception ")

            listener?.onFailure("Error : " + e.message)
        }
    }
}