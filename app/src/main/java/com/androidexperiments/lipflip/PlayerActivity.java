package com.androidexperiments.lipflip;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.VideoView;

import com.androidexperiments.lipflip.utils.AndroidUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * playback of videos
 */
public class PlayerActivity extends FragmentActivity implements AndroidUtils.OnDeleteFilesCompleteListener {
    private static final String TAG = PlayerActivity.class.getSimpleName();

    public static final String EXTRA_FILE_PATH = "extra_file_path";
    public static final String EXTRA_USE_GRID = "extra_use_grid";

    @Bind(R.id.menu_container) View mControlsContainer;
    @Bind(R.id.video_view) VideoView mVideoView;
    @Bind(R.id.play_btn) View mPlayBtn;
    @Bind(R.id.menu_grid) View mGridMenuBtn;
    @Bind(R.id.video_container) View mVideoContainer;

    /**
     * the File that we are going to show in our VideoView
     */
    private File mFileToPlay;

    /**
     * menu animations
     */
    private Animation mShowAnim, mHideAnim;

    /**
     * Need to add because VideoView and its internal MediaPlayer don't allow
     * access to checking state, so control it ourselves
     */
    private boolean mIsPaused = false;

    /**
     * play first time u arrive after create, not on every resume
     */
    private boolean mIsFirstRun = true;

    /**
     * Inflate our view and inject the things we need, as well as setup
     * our system ui stuffs
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ButterKnife.bind(this);
        init();
    }

    private void init()
    {
        //get file from activity pass
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            //this will always exist
            mFileToPlay = new File(extras.getString(EXTRA_FILE_PATH));

            //default to true unless explictly told not to
            boolean showGrid = extras.getBoolean(EXTRA_USE_GRID, true);
            mGridMenuBtn.setVisibility(showGrid ? View.VISIBLE : View.INVISIBLE);
        }

        mShowAnim = AnimationUtils.loadAnimation(this, R.anim.show_from_top);
        mHideAnim = AnimationUtils.loadAnimation(this, R.anim.hide_to_top);

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "onCompletion: " + Arrays.toString(mp.getTrackInfo()));
                showControls();
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "onError: " + what);
                return false;
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        AndroidUtils.goFullscreen(this);

        if(mIsFirstRun && mFileToPlay != null) {
            hideControls();
            playVideo(mFileToPlay);
            mIsFirstRun = false;
        }
        else
        {
            Log.d(TAG, "onResume() file: " + mFileToPlay);
            showThumbHack();
        }
    }

    private void showThumbHack()
    {
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared()");
                mVideoView.start();
                mVideoView.pause();
                mVideoView.seekTo(0);

                mVideoView.setOnPreparedListener(null);
            }
        });

        mVideoView.setVideoPath(mFileToPlay.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mVideoView.isPlaying())
        {
            onClickVideo();
        }
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @OnClick(R.id.video_view_proxy)
    public void onClickVideo() {
        Log.d(TAG, "onClickVideo() isPlaying: " + mVideoView.isPlaying() + " isPaused: " + mIsPaused);
        if (mVideoView.isPlaying()) {
            showControls();
            mVideoView.pause();
            mIsPaused = true;
        } else if (mIsPaused) {
            hideControls();
            mVideoView.start();
            mIsPaused = false;
        } else {
            mVideoView.seekTo(0);
        }
    }

    @OnClick(R.id.play_btn)
    public void onClickPlay()
    {
        hideControls();

        if(!mIsPaused)
            mVideoView.seekTo(0);

        mVideoView.start();
        mIsPaused = false;
    }

    //button navs for menu

    @OnClick(R.id.menu_back)
    public void onBackClicked()
    {
        //go back, like, duh
        onBackPressed();
    }

    @OnClick(R.id.menu_grid)
    public void onClickGrid()
    {
        Intent intent = new Intent(this, ChooserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChooserActivity.EXTRA_NEW_FILE_PATH_STRING, mFileToPlay.toString());
        startActivity(intent);
    }

    @OnClick(R.id.menu_delete)
    public void onClickDelete()
    {
        //TODO: show popup confirming, start async to delete, use util?
        Log.d(TAG, "onClickDelete() file: " + mFileToPlay);

        ArrayList<File> file = new ArrayList<>();
        file.add(mFileToPlay);

        AndroidUtils.FileDeleteTask deleteTask = new AndroidUtils.FileDeleteTask(this, file);
        deleteTask.execute();
    }

    @OnClick(R.id.menu_share)
    public void onClickShare()
    {
        Intent shareIntent = AndroidUtils.getShareIntent(mFileToPlay, null);
        startActivity(shareIntent);
    }

    //overrides

    @Override
    public void onDeleteFilesComplete(String[] filesToDelete)
    {
        Intent intent = new Intent(this, ChooserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChooserActivity.EXTRA_DELETE_FILE, filesToDelete);
        startActivity(intent);
    }

    //private api

    private void playVideo(File file)
    {
//        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
//        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), thumb);
//        mVideoContainer.setBackground(bitmapDrawable);

        mVideoView.setVideoPath(file.toString());
        mVideoView.start();
        mIsPaused = false;
    }

    private void showControls()
    {
        mPlayBtn.setVisibility(View.VISIBLE);
        mControlsContainer.setVisibility(View.VISIBLE);
        mControlsContainer.startAnimation(mShowAnim);
    }

    private void hideControls()
    {
        mPlayBtn.setVisibility(View.GONE);
        mControlsContainer.setVisibility(View.GONE);
        mControlsContainer.startAnimation(mHideAnim);
    }
}
