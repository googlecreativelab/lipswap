package com.androidexperiments.lipflip.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.androidexperiments.lipflip.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * gridview list item in ChooserActivity
 */
public class GridViewItem extends FrameLayout implements Checkable
{
    private static final String TAG = GridViewItem.class.getSimpleName();

    @Bind(R.id.thumb) ImageView mThumbnail;
    @Bind(R.id.progress) ProgressBar mProgress;

    private File mImageFile;
    private boolean mIsChecked;

    private Paint mPinkPaint;
    private ValueAnimator mHighlightAnimator;

    private float mMaxWidth = getResources().getDimensionPixelSize(R.dimen.highlight_size);

    //colors
    private int mHotPink, mTransparent;

    public GridViewItem(Context context) {
        super(context);
    }

    public GridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate()
    {
        ButterKnife.bind(this);

        init();

        super.onFinishInflate();
    }

    private void init()
    {
        this.setBackgroundResource(android.R.color.transparent);

        //minimize calls to getresources later
        mHotPink = getResources().getColor(R.color.dat_hot_pink);
        mTransparent = getResources().getColor(android.R.color.transparent);

        mPinkPaint = new Paint();
        mPinkPaint.setStyle(Paint.Style.STROKE);
        mPinkPaint.setColor(mTransparent);
        mPinkPaint.setStrokeWidth(0);
    }

    public void setImageFile(File file)
    {
        mImageFile = file;
        setImage();
    }

    public File getImageFile() {
        return mImageFile;
    }

    private void setImage()
    {
        //clear image
        mThumbnail.setImageBitmap(null);

        if(mImageFile != null)
            new ThumbLoaderTask(mThumbnail, mProgress).execute(mImageFile);
        else
            throw new RuntimeException("ImageFile is null!");
    }

    //checkable overrides

    @Override
    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }

    //internal overrides

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //keep this view square
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);

        //draw highlight on top of children
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPinkPaint);
    }


    /**
     * call when you want to animate the highlight in or out - this is NOT dependant
     * on {@link #setChecked(boolean)} above because on scroll, among other layout calls,
     * would trigger setChecked many, many times
     * @param checked
     */
    public void animateHighlight(boolean checked)
    {
        if(mHighlightAnimator != null && mHighlightAnimator.isRunning()){
            mHighlightAnimator.cancel();
            mHighlightAnimator.removeAllUpdateListeners();
            mHighlightAnimator.removeAllListeners();
        }

        if(checked) {
            mHighlightAnimator = ValueAnimator.ofFloat(0, mMaxWidth);
        }
        else {
            mHighlightAnimator = ValueAnimator.ofFloat(mMaxWidth, 0);
        }

        mHighlightAnimator.setDuration(250);
        mHighlightAnimator.addListener(mHighlightAnimatorListener);
        mHighlightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPinkPaint.setStrokeWidth((float)animation.getAnimatedValue());
                postInvalidate();
            }
        });
        mHighlightAnimator.start();
    }

    private Animator.AnimatorListener mHighlightAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mPinkPaint.setColor(mHotPink);
        }
        @Override
        public void onAnimationEnd(Animator animation) {
            /**
             * when we get down to zero (animate out) make color transparent
             * because 0 defaults to 1px stroke width
             */
            if(mPinkPaint.getStrokeWidth() == 0)
                mPinkPaint.setColor(mTransparent);
        }
        @Override
        public void onAnimationCancel(Animator animation) { }
        @Override
        public void onAnimationRepeat(Animator animation) { }
    };

    /**
     * static thumbloader async task so we dont leak the context
     */
    private static class ThumbLoaderTask extends AsyncTask<File, Void, Bitmap>
    {
        private final ImageView thumbnail;
        private final ProgressBar progress;

        public ThumbLoaderTask(ImageView mThumbnail, ProgressBar mProgress) {
            this.thumbnail = mThumbnail;
            this.progress = mProgress;
        }

        //TODO this is heavy operation (hence asynctask), we should cache thumbnails and store so we dont need to run this every single time
        @Override
        protected Bitmap doInBackground(File... params)
        {
            return ThumbnailUtils.createVideoThumbnail(params[0].toString(), MediaStore.Images.Thumbnails.MINI_KIND);
        }

        @Override
        protected void onPostExecute(Bitmap bmp)
        {
            AlphaAnimation alpha = new AlphaAnimation(0.f, 1.f);
            alpha.setDuration(250);

            thumbnail.setImageBitmap(bmp);
            thumbnail.startAnimation(alpha);

            progress.setVisibility(View.GONE);
        }
    }

}
