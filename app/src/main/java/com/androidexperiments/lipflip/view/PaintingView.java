package com.androidexperiments.lipflip.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.androidexperiments.lipflip.R;

/**
 * handles painting onto screen
 */
public class PaintingView extends View
{
    private static final String TAG = PaintingView.class.getSimpleName();

    private Bitmap mMainBitmap;
    private Canvas mMainCanvas;

    private Bitmap mBitmapBrush;
    private Point mBitmapBrushDimensions;
    private Bitmap mDrawingCache = null;

    public PaintingView(Context context) {
        super(context);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        mBitmapBrush = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.brush_small);
        mBitmapBrushDimensions = new Point(mBitmapBrush.getWidth(), mBitmapBrush.getHeight());

        this.setDrawingCacheEnabled(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mMainBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mMainCanvas = new Canvas(mMainBitmap);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                invalidate();
                break;
        }
        return true;
    }

    private void touchStart(float x, float y)
    {
        Log.d(TAG, "touchStart() " + x + ", " + y);
    }

    private void touchMove(float x, float y)
    {
        mMainCanvas.drawBitmap(mBitmapBrush, Math.round(x - mBitmapBrushDimensions.x / 2), Math.round(y - mBitmapBrushDimensions.y / 2), null);
    }

    private void touchUp(float x, float y)
    {
        Log.d(TAG, "touchUp() " + x + ", " + y);

        if(mOnNewBitmapReadyListener != null)
            mOnNewBitmapReadyListener.onNewBitmapReady(mMainBitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        //set width and height the same
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(mMainBitmap != null) {
            canvas.drawBitmap(mMainBitmap, 0, 0, null);
        }
    }

    public Bitmap getDrawingCopy()
    {
        return mMainBitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    public void clear()
    {
        mMainCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    /**
     * interface for grabbing the new image cache whenever its ready from touch up
     */
    public interface OnNewBitmapReadyListener {
        public void onNewBitmapReady(Bitmap bitmap);
    }

    private OnNewBitmapReadyListener mOnNewBitmapReadyListener;

    public void setOnNewBitmapReadyListener(OnNewBitmapReadyListener listener)
    {
        mOnNewBitmapReadyListener = listener;
    }
}
