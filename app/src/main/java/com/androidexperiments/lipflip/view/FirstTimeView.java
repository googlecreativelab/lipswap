package com.androidexperiments.lipflip.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.androidexperiments.lipflip.R;

/**
 * first time!
 */
public class FirstTimeView extends FrameLayout
{
    public FirstTimeView(Context context) {
        super(context);
        init();
    }

    public FirstTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FirstTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_first_time, this);

        this.setBackgroundColor(0xcc000000);
    }

    public void animateOut()
    {
        AlphaAnimation anim = new AlphaAnimation(1.f, 0.f);
        anim.setDuration(350);
        this.startAnimation(anim);
        this.setVisibility(GONE);
    }
}
