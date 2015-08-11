package com.androidexperiments.lipflip.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.androidexperiments.lipflip.R;
import com.androidexperiments.lipflip.utils.SimpleAnimationListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Intro view for easier handling of video view
 */
public class IntroView extends RelativeLayout
{
    @Bind(R.id.logo) VideoView mLogoVideo;
    @Bind(R.id.intro_text_title)    View mIntroTextTitle;
    @Bind(R.id.intro_text_bottom)    View mIntroTextBottom;

    public IntroView(Context context) {
        super(context);
    }

    public IntroView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this, this);

        if(!isInEditMode())
        {
            mIntroTextTitle.setVisibility(GONE);
            mIntroTextBottom.setVisibility(GONE);
        }

        setupVideo();
    }

    private void setupVideo() {
        String path = "android.resource://" + getContext().getPackageName() + "/" + R.raw.intro;
        mLogoVideo.setVideoURI(Uri.parse(path));
        mLogoVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                showText();
            }
        });
    }

    private void showText()
    {
        //prep for image
//        this.setDrawingCacheEnabled(true);

        AlphaAnimation alpha = new AlphaAnimation(0.f, 1.f);
        alpha.setDuration(250);
        alpha.setAnimationListener(new SimpleAnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                animationComplete();
            }
        });

        mIntroTextTitle.startAnimation(alpha);
        mIntroTextBottom.startAnimation(alpha);

        mIntroTextTitle.setVisibility(VISIBLE);
        mIntroTextBottom.setVisibility(VISIBLE);
    }

    private void animationComplete()
    {
//        this.setBackground(new BitmapDrawable(getResources(), getDrawingCache()));
//        this.removeAllViews();

        if(mOnIntroCompleteListener != null)
            mOnIntroCompleteListener.onIntroComplete();
    }

    public void startIntro(OnIntroCompleteListener listener)
    {
        mOnIntroCompleteListener = listener;

        mLogoVideo.start();
    }

    public interface OnIntroCompleteListener {
        void onIntroComplete();
    }

    private OnIntroCompleteListener mOnIntroCompleteListener;

    public void setOnIntroCompleteListener(OnIntroCompleteListener listener) {
        this.mOnIntroCompleteListener = listener;
    }
}
