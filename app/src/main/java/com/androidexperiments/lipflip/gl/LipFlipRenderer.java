package com.androidexperiments.lipflip.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.gl.CameraRenderer;


/**
 * lip flip
 *
 * add two textures on top of the camera texture created in renderer,
 * one of an image, which can be taken from camera roll, or from
 * any app capable of sharing images via intent. second texture
 * is used as a mask, which is drawn by your finger
 */
public class LipFlipRenderer extends CameraRenderer
{
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

    public LipFlipRenderer(Context context, SurfaceTexture texture, CameraFragment cameraFragment, int width, int height)
    {
        //use new vert and frag shaders
        super(context, texture, cameraFragment, width, height, "lip_service.frag", "lip_service.vert");
    }

    @Override
    protected void onSetupComplete()
    {
        //add the image that was chosen by the user - match with sampler2D in frag shader
        addTexture(mInitialBitmap, "faceTexture");

        if(mPaintTexId == -1)
            mPaintTexId = addTexture(mNextCache, UNIFORM_PAINT_TEX);

        super.onSetupComplete();
    }

    @Override
    protected void deinitGLComponents()
    {
        mPaintTexId = -1;

        super.deinitGLComponents();
    }

    /**
     * used only on first run
     * @param bitmap
     */
    public void setPaintTexture(Bitmap bitmap) {
        mNextCache = bitmap;
    }

    public void updatePaintTexture(Bitmap drawingCache)
    {
        mNextCache = drawingCache;
        mUpdateIsNeeded = true;
    }

    @Override
    protected void setExtraTextures()
    {
        if(mUpdateIsNeeded)
        {
            try {
                updateTexture(mPaintTexId, mNextCache);
                mBitmapHandler.sendEmptyMessage(MSG_UPDATE_COMPLETE);
            }
            catch(IllegalArgumentException e)
            {
                //first run and awful way to hope this fails
            }

            mUpdateIsNeeded = false;
        }
        super.setExtraTextures();
    }

    @Override
    protected void setUniformsAndAttribs()
    {
        super.setUniformsAndAttribs();

        int imgAspectRatioHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "imgAspectRatio");
        GLES20.glUniform1f(imgAspectRatioHandle, mImageAspectRatio);

        int gammaHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "gamma");
        GLES20.glUniform1f(gammaHandle, mGamma);

        int hueHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "hue");
        GLES20.glUniform1f(hueHandle, mHue);
    }

    /**
     * update the image alpha uniform from edit slider
     * @param gamma
     */
    public void setGamma(float gamma)
    {
        mGamma = gamma;
    }

    /**
     * update the color tone from edit slider
     */
    public void setHue(float hue)
    {
        mHue = hue;
    }

    /**
     * set the handler responsible for passing back update complete message
     * @param bitmapHandler
     */
    public void setBitmapHandler(Handler bitmapHandler) {
        mBitmapHandler = bitmapHandler;
    }

    public void setInitialBitmap(Bitmap initialBitmap)
    {
        if(initialBitmap == null)
            throw new IllegalArgumentException("initialBitmap cannot be null!");

        mInitialBitmap = initialBitmap;

        int imgWidth = initialBitmap.getWidth();
        int imgHeight = initialBitmap.getHeight();

        mImageAspectRatio = (float)imgWidth / imgHeight;

        //todo - rename to what this really is?

        if(mImageAspectRatio == mSurfaceAspectRatio)
            mImageAspectRatio = 1.f;
        else if(mImageAspectRatio > mSurfaceAspectRatio)
        {
            float nw = ((float)mSurfaceHeight / imgHeight) * imgWidth;
            mImageAspectRatio = mSurfaceWidth / nw;
        }
        else
        {
            float nh = ((float)mSurfaceWidth / imgWidth) * imgHeight;
            mImageAspectRatio = mSurfaceHeight / nh;
        }
    }

}
