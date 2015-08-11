package com.androidexperiments.lipflip;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * last minute request for about and licenses to be added - activity time!
 */
public class TextActivity extends AppCompatActivity {
    public static final String TYPE = "type";
    public static final String TYPE_ABOUT = "type_about";
    public static final String TYPE_LICENSE = "type_license";

    private TextView mTitleText;
    private WebView mMainWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_text);

        mMainWebView = (WebView)findViewById(R.id.main_web_view);

        setupActionBar();
        setupText();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.view_actionbar);
            mTitleText = (TextView) actionBar.getCustomView().findViewById(R.id.actionbar_text_view);
        }
    }

    private void setupText() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String type = extras.getString(TYPE);
            if(type != null) {
                switch (type) {
                    case TYPE_ABOUT:
                        mMainWebView.loadUrl("file:///android_res/raw/about.html");
                        mTitleText.setText("ABOUT");
                        break;
                    case TYPE_LICENSE:
                        mMainWebView.loadUrl("file:///android_res/raw/licenses.html");
                        mTitleText.setText("LICENSES");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String getStringHtmlStringFromRaw(int raw_id) {
        InputStream inputStream = getResources().openRawResource(raw_id);
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
