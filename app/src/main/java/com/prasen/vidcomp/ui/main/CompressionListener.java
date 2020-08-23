package com.prasen.vidcomp.ui.main;

public interface CompressionListener {

    void compressionFinished(int status, boolean isVideo, String fileOutputPath);

    void onFailure(String message);

    void onProgress(int progress);

}
