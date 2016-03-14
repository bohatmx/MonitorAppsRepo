package com.boha.monitor.library.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import com.boha.monitor.library.activities.AutoFitTextureView;
import com.boha.monitor.library.activities.Camera2VideoFragment;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.MonLog;

import java.util.concurrent.Semaphore;

/**
 * Created by aubreymalabie on 3/13/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VideoCameraFragment  extends Fragment {
    private static final String LOG = "Camera2VideoFragment";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private AutoFitTextureView mTextureView;
    private Button mButtonVideo;
    private CameraCaptureSession mPreviewSession;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
//            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
//            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };
    private Size mPreviewSize;
    private Size mVideoSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecordingVideo;
    private HandlerThread mBackgroundThread;
    private CameraDevice mCameraDevice;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraListener listener;
    ProjectDTO project;

    public void setListener(CameraListener listener) {
        this.listener = listener;
    }

    public interface CameraListener {
        void onVideoCompleted(VideoUploadDTO video);

        void onError();
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            MonLog.d(getActivity(), LOG, "---- onOpened -------");
            mCameraDevice = cameraDevice;
//            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
//                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            MonLog.d(getActivity(), LOG, "---- onDisconnected -------");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            MonLog.d(getActivity(), LOG, "---- onError -------: " + cameraDevice.toString());
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    public static Camera2VideoFragment newInstance(ProjectDTO p) {
        Camera2VideoFragment fragment = new Camera2VideoFragment();
        Bundle b = new Bundle();
        b.putSerializable("project", p);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle b) {
        if (b != null) {
            project = (ProjectDTO) b.getSerializable("project");
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            project = (ProjectDTO) bundle.getSerializable("project");

        }
        super.onCreate(b);
    }


}
