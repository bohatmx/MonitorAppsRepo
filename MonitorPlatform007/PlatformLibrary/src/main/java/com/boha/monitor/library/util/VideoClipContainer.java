package com.boha.monitor.library.util;

import com.boha.monitor.library.dto.VideoClipDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/24.
 */
public class VideoClipContainer implements Serializable {

    List<VideoClipDTO> videoClips = new ArrayList<VideoClipDTO>();

    public void addVideo(VideoClipDTO clip) {
        videoClips.add(clip);
    }

    public List<VideoClipDTO> getVideoClips() {
        return videoClips;
    }

    public void setVideoClips(List<VideoClipDTO> videoClips) {
        this.videoClips = videoClips;
    }

}
