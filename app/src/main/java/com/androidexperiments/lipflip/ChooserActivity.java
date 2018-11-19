package com.androidexperiments.lipflip;

import com.androidexperiments.lipflip.adapters.ActualArrayAdapter;
import com.androidexperiments.lipflip.data.Constants;
import com.androidexperiments.lipflip.utils.AndroidUtils;
import com.androidexperiments.lipflip.utils.FileUtils;
import com.androidexperiments.lipflip.utils.SimpleAnimationListener;
import com.androidexperiments.lipflip.view.GridViewItem;
import com.androidexperiments.lipflip.view.IntroView;
import com.androidexperiments.shadercam.fragments.PermissionsHelper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ChooserActivity extends AppCompatActivity
        implements AndroidUtils.OnDeleteFilesCompleteListener,
        PermissionsHelper.PermissionsListener {

    private static final String TAG = ChooserActivity.class.getSimpleName();

    /**
     * request code for when we choose pic from camera or documents
     */
    private static final int REQUEST_CODE_SELECT_PIC = 100;

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    /**
     * used when coming explicitly to this activity from PlayerActivity after creating
     * a new Lip Flip that will be added to the grid
     */
    public static final String EXTRA_NEW_FILE_PATH_STRING = "extra_new_file_path_string";

    public static final String EXTRA_DELETE_FILE = "extra_delete_file";

    @Bind(R.id.intro_view)
    IntroView mIntroView;

    @Bind(R.id.list_chooser)
    GridView mGridView;

    @Bind(R.id.progress_loader)
    ProgressBar mProgressLoader;

    @Bind(R.id.text_get_started)
    TextView mGetStartedText;

    @Bind(R.id.btn_create_new)
    ImageButton mBtnCreateNew;

    @Bind(R.id.photo_chooser_container)
    ViewGroup mPhotoChooserContainer;

    @Bind(R.id.photo_chooser_background)
    ViewGroup mPhotoChooserBackground;

    /**
     * base adapter that the animation adapter wraps
     */
    private ChooserAdapter mChooserAdapter;

    private Uri outputFileUri;

    private Animation mShowFromBottom, mHideFromBottom;

    /**
     * Only play intro video when its first time
     */
    private boolean mIsFirstRun;

    private String[] mFilesToDelete = null;

    private PermissionsHelper mPermissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        ButterKnife.bind(this);

        mIsFirstRun = true;

        setupGridView();
        setupActionBar();
        setupEdit();
        setupAnimations();
    }

    private void setupGridView() {
        mGridView.setOnItemClickListener(mOnItemClickListener);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setMultiChoiceModeListener(
                new OnMultiChoiceListener(ChooserActivity.this, mGridView));
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.view_actionbar);
            actionBar.setShowHideAnimationEnabled(true);
        }
    }

    private void setupEdit() {
        if (!mPhotoChooserContainer.isInEditMode()) {
            mPhotoChooserBackground.setVisibility(View.GONE);
            mPhotoChooserContainer.setVisibility(View.GONE);
        }
    }

    private void setupAnimations() {
        mShowFromBottom = AnimationUtils.loadAnimation(this, R.anim.show_from_bottom);
        mHideFromBottom = AnimationUtils.loadAnimation(this, R.anim.hide_to_bottom);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outputFileUri != null) {
            outState.putString("cameraImageUri", outputFileUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            outputFileUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    /**
     * setup for calligraphy lib
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_license:
                showLicense();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * handle adding a new file to the list when we come back from a gridicon click
     * from the player view post-adding
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            //check for new file
            String newFilePath = intent.getExtras().getString(EXTRA_NEW_FILE_PATH_STRING);
            if (newFilePath != null) {
                //first run/no files added, adapter is empty and non-existant
                if (mChooserAdapter == null) {
                    new GetLipFlipsTask(this).execute();
                } else {
                    //we have file from playeractivity presumably, add it to the top of list!
                    File newFile = new File(newFilePath);
                    mChooserAdapter.add(0, newFile);
                    checkItems();

                    //scan for lib
                    scanMedia(new String[]{newFilePath});
                }
                return;
            }

            //will only be one file but comes conveniently as string array already
            //but we need to wait until onResume to do that
            mFilesToDelete = intent.getExtras().getStringArray(EXTRA_DELETE_FILE);

            if (mFilesToDelete != null && mFilesToDelete.length > 0) {
                //we already have method to handle this - so handle it already, will ya!
                onDeleteFilesComplete(mFilesToDelete);
            }
        }
    }

    /**
     * callback from the {@link .ChooserActivity.GetLipFlipsTask } when it completes
     */
    private void onGetLipFlipsComplete(ChooserAdapter adapter) {
        mProgressLoader.setVisibility(View.GONE);

        if (adapter != null) {
            mGetStartedText.setVisibility(View.GONE);

            if (mChooserAdapter != null && (adapter.getCount() == mChooserAdapter.getCount())) {
                //same images (presumably) as last time, so dont update
                return;
            }

            mGridView.setAdapter(adapter);
            mChooserAdapter = adapter;
        } else {
            mGetStartedText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * scan specific media to make sure it shows up in photos
     */
    private void scanMedia(String[] files) {
        MediaScannerConnection.scanFile(
                this,
                files,
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(TAG, "file " + path + " was scanned successfully: " + uri);
                    }
                }
        );
    }

    /**
     * fade out for intro after video and text finished animating
     */
    private void startIntro() {
        final Handler handler = new Handler();

        /**
         * responsible for fading out intro view and moving on with list setup
         */
        final Runnable introFade = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Animation alpha = new AlphaAnimation(1.0f, 0.f);
                        alpha.setDuration(750);
                        alpha.setAnimationListener(new SimpleAnimationListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                //remove shit we dont need
                                ((ViewGroup) mIntroView.getParent()).removeView(mIntroView);

                                //animate button in
                                mBtnCreateNew.setVisibility(View.VISIBLE);
                                mBtnCreateNew.startAnimation(mShowFromBottom);

                                //show actionbar
                                getSupportActionBar().show();

                                //create dat list
                                setupList();
                            }
                        });

                        mIntroView.startAnimation(alpha);
                        mIntroView.setVisibility(View.GONE);

                        //animate in
                        mBtnCreateNew.setVisibility(View.GONE);
                    }
                });
            }
        };

        //start video and animation, calling introFade runnable when we're done
        mIntroView.startIntro(new IntroView.OnIntroCompleteListener() {
            @Override
            public void onIntroComplete() {
                handler.postDelayed(introFade, 1000);
            }
        });
    }

    /**
     * handle everything in an AsycTask because file i/o sometimes takes a while, don't want to lock UI
     */
    private void setupList() {
        mGetStartedText.setVisibility(View.VISIBLE);
        mProgressLoader.setVisibility(View.VISIBLE);
        new GetLipFlipsTask(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsFirstRun) {

            if (PermissionsHelper.isMorHigher()) {
                setupPermissions();
            } else {
                startIntro();
                mIsFirstRun = false;
            }
        } else {
            //every time we come back from anywhere, check to see if anything is gone/added
            //issue 52 from not following original ux flow, poor design
            new GetLipFlipsTask(this).execute();
        }
    }

    /**
     * Handle click on big plus sign
     */
    @OnClick(R.id.btn_create_new)
    public void onCreateNewClick() {
        mPhotoChooserContainer.startAnimation(mShowFromBottom);
        mPhotoChooserContainer.setVisibility(View.VISIBLE);
        mBtnCreateNew.startAnimation(mHideFromBottom);
        mBtnCreateNew.setVisibility(View.GONE);

        AlphaAnimation alphaIn = new AlphaAnimation(0.f, 1.f);
        alphaIn.setDuration(mShowFromBottom.getDuration());
        mPhotoChooserBackground.startAnimation(alphaIn);
        mPhotoChooserBackground.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.photo_chooser_background)
    public void onBackgroundClick() {
        mPhotoChooserContainer.startAnimation(mHideFromBottom);
        mPhotoChooserContainer.setVisibility(View.GONE);

        mBtnCreateNew.startAnimation(mShowFromBottom);
        mBtnCreateNew.setVisibility(View.VISIBLE);

        AlphaAnimation alphaOut = new AlphaAnimation(1.f, 0.f);
        alphaOut.setDuration(mHideFromBottom.getDuration());
        mPhotoChooserBackground.startAnimation(alphaOut);
        mPhotoChooserBackground.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_use_camera)
    public void onClickUseCameraBtn() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        FileUtils.cleanUpFileStubs();

    }

    private void setupPermissions() {
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        );
        mPermissionsHelper.checkAppCompatPermissions(this);
    }


    @OnClick(R.id.btn_use_docs)
    public void onClickUseDocs() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(galleryIntent, REQUEST_CODE_SELECT_PIC);
    }


    /**
     * handle our choosing of a photo above, pushing the image into our {@link .LipServiceActivity}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_CODE_SELECT_PIC) {
                Log.d(TAG, "image success! " + outputFileUri);

                final boolean isCamera;
                String action = "";

                if (data == null) {
                    isCamera = true;
                } else {
                    action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }

                //weird bug if its still null with no info
                //TODO - why does our resultCode == SUCCESS but intent data is null only on some phones?
                if (selectedImageUri == null && outputFileUri != null) {
                    selectedImageUri = outputFileUri;
                }

                //todo dont animate this
                onBackgroundClick();

                Intent intent = new Intent(this, LipSwapActivity.class);
                intent.putExtra(LipSwapActivity.EXTRA_URI, selectedImageUri);
                startActivity(intent);
            }
        }
    }

    private Uri getImageUri() {
        final File root = Constants.getStorageDir(this);
        root.mkdirs();
        final String fname = "lip_img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = FileProvider
                .getUriForFile(this,
                        this.getApplicationContext().getPackageName() + ".utils.provider",
                        sdImageMainDirectory);

        return outputFileUri;
    }

    /**
     * delete all items with corresponding ids, in separate task, finishing mode after if necessary
     */
    private void deleteItems(long[] checkedItemIds, ActionMode mode) {
        ArrayList<File> itemsToRemove = new ArrayList<>();
        for (long id : checkedItemIds) {
            itemsToRemove.add(mChooserAdapter.getItem((int) id));
        }

        AndroidUtils.FileDeleteTask deleteTask = new AndroidUtils.FileDeleteTask(this,
                itemsToRemove);
        deleteTask.execute();
    }

    /**
     * when our task is done deleting stuffs, cleanup whatever we need to.
     * called from {@link AndroidUtils.FileDeleteTask}
     */
    @Override
    public void onDeleteFilesComplete(String[] filesToDelete) {
        if (filesToDelete.length == 0 || mChooserAdapter == null) {
            return;
        }

        //remove from adapter - have to create array from array, yay
        ArrayList<File> files = new ArrayList<>();
        for (String file : filesToDelete) {
            files.add(new File(file));
        }
        mChooserAdapter.removeAll(files);

        //scan gone and check edit
        scanMedia(filesToDelete);
        checkItems();
    }

    /**
     * if post-updated list is empty, mShowFromBottom our entry screen again!
     */
    private void checkItems() {
        if (mChooserAdapter.getCount() == 0) {
            mGetStartedText.setVisibility(View.VISIBLE);
        } else {
            mGetStartedText.setVisibility(View.GONE);
        }
    }

    private void showAbout() {
        Intent intent = new Intent(this, TextActivity.class);
        intent.putExtra(TextActivity.TYPE, TextActivity.TYPE_ABOUT);
        startActivity(intent);
    }

    private void showLicense() {
        Intent intent = new Intent(this, TextActivity.class);
        intent.putExtra(TextActivity.TYPE, TextActivity.TYPE_LICENSE);
        startActivity(intent);
    }

    //getters to help

    public ChooserAdapter getChooserAdapter() {
        return mChooserAdapter;
    }

    //classes and stuffs

    /**
     * handles the single click of a list item, pushing file into PlayerActivity and letting
     * it know that we don't need the Grid icon to be available (back works fine)
     */
    private AdapterView.OnItemClickListener mOnItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "item: " + parent.getAdapter().getItem(position));

            Intent intent = new Intent(ChooserActivity.this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_USE_GRID, false);
            intent.putExtra(PlayerActivity.EXTRA_FILE_PATH,
                    (parent.getAdapter().getItem(position)).toString());
            startActivity(intent);
        }
    };

    @Override
    public void onPermissionsSatisfied() {
        startIntro();
        mIsFirstRun = false;
    }

    @Override
    public void onPermissionsFailed(String[] strings) {
        finish();
    }

    /**
     * gridview listener for multiple choice listeners, static with weak activity ref for callbacks
     */
    private static class OnMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

        private final GridView mGridView;

        private final WeakReference<ChooserActivity> mWeakActivity;

        private List<GridViewItem> mSelectedItems = new ArrayList<>();

        public OnMultiChoiceListener(ChooserActivity activity, GridView gridView) {
            mWeakActivity = new WeakReference<>(activity);
            mGridView = gridView;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            //highlight stuff if it should be checked
            GridViewItem item = (GridViewItem) mGridView
                    .getChildAt(position - mGridView.getFirstVisiblePosition());
            item.animateHighlight(checked);

            Log.d(TAG, "onItemCheckedStateChanged() " + position + " id: " + id + " checked: "
                    + checked);

            if (checked) {
                mSelectedItems.add(item);
            } else {
                mSelectedItems.remove(item);
            }

            if (mSelectedItems.size() > 1) {
                //more than one selected, so disable sharing
                mode.getMenu().findItem(R.id.action_share).setEnabled(false);
            } else if (mSelectedItems.size() == 1) {
                mode.getMenu().findItem(R.id.action_share).setEnabled(true);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "onCreateActionMode()");
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_selected_items, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ChooserAdapter adapter = mWeakActivity.get().getChooserAdapter();

            switch (item.getItemId()) {
                case R.id.action_share:
                    Intent shareIntent = AndroidUtils.getShareIntent(item.getActionView().getContext(),
                            adapter.getItem(
                                    (int) mGridView.getCheckedItemIds()[0]
                                    //first object since we can only have one selected at a time
                            ),
                            null //insert "Made by Lip Flip?"
                    );
                    mWeakActivity.get().startActivity(shareIntent);
                    break;

                case R.id.action_delete:
                    Log.d(TAG, "onActionItemClicked(){delete}"
                            + " num: " + mGridView.getCheckedItemCount()
                            + " ids: " + Arrays.toString(mGridView.getCheckedItemIds()));

                    mWeakActivity.get().deleteItems(mGridView.getCheckedItemIds(), mode);

                    //finish mode regardless of outcome...? bad idea?
                    mode.finish();
                    break;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mSelectedItems.size() > 0) {
                for (GridViewItem item : mSelectedItems) {
                    item.animateHighlight(false);
                }

                mSelectedItems.clear();
            }
        }
    }

    /**
     * task to get all the files in the LipFlip dir and create adapter
     */
    public static class GetLipFlipsTask extends AsyncTask<Void, Void, ChooserAdapter> {

        private final WeakReference<ChooserActivity> weakActivity;

        public GetLipFlipsTask(ChooserActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected ChooserAdapter doInBackground(Void... params) {
            List<File> files = Constants.getAllLipFlipVideos(weakActivity.get());

            ChooserAdapter adapter = null;

            if (files != null && files.size() > 0) {
                adapter = new ChooserAdapter(weakActivity.get(), files);
            }

            return adapter;
        }

        @Override
        protected void onPostExecute(ChooserAdapter adapter) {
            weakActivity.get().onGetLipFlipsComplete(adapter);
        }
    }

    /**
     * custom adapter for our grid view
     */
    private static class ChooserAdapter extends ActualArrayAdapter<File> {

        private final Context mContext;

        ChooserAdapter(Context ctx, List<File> items) {
            super(items);
            mContext = ctx;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridViewItem item = (GridViewItem) convertView;

            if (item == null) {
                //cheating
                item = (GridViewItem) LayoutInflater.from(mContext)
                        .inflate(R.layout.view_grid_item, parent, false);

                ItemHolder holder = new ItemHolder(getItem(position));
                item.setTag(holder);
            }

            Log.d(TAG, "getView() " + position);

            ItemHolder holder = (ItemHolder) item.getTag();

            File nextFile = getItem(position);

            if (item.getImageFile() == null || !holder.file.equals(nextFile)) {
                item.setImageFile(nextFile);
                holder.file = nextFile;
                item.setTag(holder);
            }

            return item;
        }

        private class ItemHolder {

            File file;

            public ItemHolder(File file) {
                this.file = file;
            }
        }

        /**
         * necessary for {@link OnMultiChoiceListener} to know checked id's
         *
         * @return true because super impl is always false
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
