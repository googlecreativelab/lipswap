package com.androidexperiments.lipflip.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

/**
 * Since {@link android.widget.VideoView} is garbage and based off SurfaceView,
 * we extend {@link TextureView} so that we can have scaling, alpha, etc
 */
public class VideoView extends TextureView implements TextureView.SurfaceTextureListener
{
    private static final String TAG = VideoView.class.getSimpleName();

    /**
     * Surface used for rendering MediaPlayer content
     */
    private Surface mSurface;

    /**
     * MediaPlayer to handle all the dirty work
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Uri pointing to the video we want to play
     */
    private Uri mVideoUri;

    /**
     * Flag to start video ASAP if start is called before surface is ready
     */
    private boolean mPlayWhenReady = false;

    /**
     * Listener passed in for usage with our internal listener
     */
    private MediaPlayer.OnCompletionListener mOnCompletionListener;


    /**
     * Main Constructor
     */
    public VideoView(Context context) {
        super(context);
        init();
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Get our surface texture listener as soon as possible
     */
    private void init() {
        this.setSurfaceTextureListener(this);
    }

    /**
     * set the Uri of the video we want to play
     * @param videoUri
     */
    public void setVideoURI(Uri videoUri)
    {
        mVideoUri = videoUri;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        mOnCompletionListener = listener;
    }

    public void start()
    {
        if(mVideoUri == null) {
            throw new RuntimeException("VideoUri null. Call setVideoURI first!");
        }

        if(mSurface == null) {
            mPlayWhenReady = true;
            return;
        }

        if(mMediaPlayer == null) {
            setMediaPlayer();
        }

        mMediaPlayer.start();
    }

    public void stop(){
        if(mMediaPlayer == null)
            return;

        mMediaPlayer.stop();
    }

    private void setMediaPlayer()
    {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        try {
            mMediaPlayer= new MediaPlayer();
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setDataSource(getContext(), mVideoUri);
            mMediaPlayer.setOnCompletionListener(mInternalOnCompletionListener);
            mMediaPlayer.prepare();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);

        if(mPlayWhenReady) {
            start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    /**
     * Our internal listener to pass events along and do cleanup with
     */
    private MediaPlayer.OnCompletionListener mInternalOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //forward event along
            if(mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mp);
        }
    };
}
