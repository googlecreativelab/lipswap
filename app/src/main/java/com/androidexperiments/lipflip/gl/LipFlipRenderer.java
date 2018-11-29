package com.androidexperiments.lipflip.gl;

import com.androidexperiments.shadercam.gl.VideoRenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * lip flip
 *
 * add two textures on top of the camera texture created in renderer,
 * one of an image, which can be taken from camera roll, or from
 * any app capable of sharing images via intent. second texture
 * is used as a mask, which is drawn by your finger
 */
public class LipFlipRenderer extends VideoRenderer {

    private static final String TAG = LipFlipRenderer.class.getSimpleName();

    private static final String UNIFORM_PAINT_TEX = "paintTexture";

    public static final int MSG_UPDATE_COMPLETE = 10;

    private int mPaintTexId = -1;

    private Bitmap mNextCache;

    private boolean mUpdateIsNeeded = false;

    private Handler mBitmapHandler;

    private Bitmap mInitialBitmap;

    private float mGamma = 1.0f;

    private float mHue = 0.f;

    private float mImageAspectRatio = 1.f;

    protected float mSurfaceAspectRatio;

    private float[] mFaceOrtho = new float[16];

    private float mFaceScreenAspect = 1.0f;

    private int mScreenFaceHandle;

    public LipFlipRenderer(Context context, int width, int height) {
        //use new vert and frag shaders
        super(context, "lip_service.frag", "lip_service.vert");
        mSurfaceAspectRatio = ((1.0f * width) / (1.0f * height));
        mSurfaceHeight = height;
        mSurfaceWidth = width;
    }

    @Override
    protected void onSetupComplete() {
        //add the image that was chosen by the user - match with sampler2D in frag shader
        if (mInitialBitmap != null) {
            addTexture(mInitialBitmap, "faceTexture");
        }
        if (mPaintTexId == -1) {
            mPaintTexId = addTexture(mNextCache, UNIFORM_PAINT_TEX);
        }

        super.onSetupComplete();
    }

    @Override
    protected void deinitGLComponents() {
        mPaintTexId = -1;

        super.deinitGLComponents();
    }

    /**
     * used only on first run
     */
    public void setPaintTexture(Bitmap bitmap) {
        mNextCache = bitmap;
    }

    public void updatePaintTexture(Bitmap drawingCache) {
        mNextCache = drawingCache;
        mUpdateIsNeeded = true;
    }

    @Override
    public void onDrawFrame() {

        android.opengl.Matrix
                .orthoM(mFaceOrtho, 0, -mFaceScreenAspect, mFaceScreenAspect,  -mFaceScreenAspect,  mFaceScreenAspect ,-1, 1);

        super.onDrawFrame();
    }

    @Override
    protected void setExtraTextures() {
        if (mUpdateIsNeeded) {
            try {
                updateTexture(mPaintTexId, mNextCache);
                mBitmapHandler.sendEmptyMessage(MSG_UPDATE_COMPLETE);
            } catch (IllegalArgumentException e) {
                //first run and awful way to hope this fails
                Log.e(TAG, "ILLEGAL", e);
            }

            mUpdateIsNeeded = false;
        }
        super.setExtraTextures();
    }

    @Override
    protected void setUniformsAndAttribs() {
        super.setUniformsAndAttribs();

        int imgAspectRatioHandle = GLES20
                .glGetUniformLocation(mCameraShaderProgram, "imgAspectRatio");
        GLES20.glUniform1f(imgAspectRatioHandle, mImageAspectRatio);

        int gammaHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "gamma");
        GLES20.glUniform1f(gammaHandle, mGamma);

        int hueHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "hue");
        GLES20.glUniform1f(hueHandle, mHue);

        mScreenFaceHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "FMatrix");

        GLES20.glUniformMatrix4fv(mScreenFaceHandle, 1, false, mFaceOrtho, 0);


    }

    /**
     * update the image alpha uniform from edit slider
     */
    public void setGamma(float gamma) {
        mGamma = gamma;
    }

    /**
     * update the color tone from edit slider
     */
    public void setHue(float hue) {
        mHue = hue;
    }

    /**
     * set the handler responsible for passing back update complete message
     */
    public void setBitmapHandler(Handler bitmapHandler) {
        mBitmapHandler = bitmapHandler;
    }

    public void setInitialBitmap(Bitmap initialBitmap) {
        if (initialBitmap == null) {
            throw new IllegalArgumentException("initialBitmap cannot be null!");
        }

        float surfaceAspect = mSurfaceWidth / (1.0f * mSurfaceHeight);

        Bitmap tempBitmap = ThumbnailUtils.extractThumbnail(initialBitmap, mSurfaceWidth, mSurfaceHeight);

        mInitialBitmap = tempBitmap;

        int imgWidth = mInitialBitmap.getWidth();
        int imgHeight = mInitialBitmap.getHeight();

        mImageAspectRatio = imgWidth / (1.0f * imgHeight);

        mFaceScreenAspect = surfaceAspect/ mImageAspectRatio;

    }

}
