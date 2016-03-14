package com.boha.monitor.library.util.bean;

/**
 * Created by aubreymalabie on 3/13/16.
 */
public class VideoFileException extends Exception {
    String mMessage;

    public VideoFileException() {
    }
    public VideoFileException(String message) {
        mMessage = message;
    }

    public String getVideoFileMessage() {
        return mMessage;
    }
}
