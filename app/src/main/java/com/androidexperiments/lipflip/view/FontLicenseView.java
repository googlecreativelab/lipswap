package com.androidexperiments.lipflip.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.androidexperiments.lipflip.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Webview controls for loading text
 */
public class FontLicenseView extends FrameLayout
{
    @Bind(R.id.font_license_webview)    WebView mFontLicenseWebview;

    public FontLicenseView(Context context) {
        super(context);
    }

    public FontLicenseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontLicenseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this, this);

        mFontLicenseWebview.loadUrl("file:///android_res/raw/font_license.txt");
    }
}
