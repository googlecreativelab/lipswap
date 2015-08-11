package com.androidexperiments.lipflip.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.androidexperiments.lipflip.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * General i/o helpers and such
 */
public class AndroidUtils
{
    /**
     * Helper for getting strings from any file type in /assets/ folder. Primarily used for shaders.
     *
     * @param ctx Context to use
     * @param filename name of the file, including any folders, inside of the /assets/ folder.
     * @return String of contents of file, lines separated by <code>\n</code>
     * @throws java.io.IOException if file is not found
     */
    public static String getStringFromFileInAssets(Context ctx, String filename) throws IOException {
        return getStringFromFileInAssets(ctx, filename, true);
    }

    public static String getStringFromFileInAssets(Context ctx, String filename, boolean useNewline) throws IOException
    {
        InputStream is = ctx.getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            builder.append(line + (useNewline ? "\n" : ""));
        }
        is.close();
        return builder.toString();
    }

    /**
     * get a dialog to show while we create/add/delete all kinds of stuff
     * @param ctx
     * @param textId
     * @return
     */
    public static Dialog getAlertDialog(Context ctx, int textId)
    {
        View alertView = LayoutInflater.from(ctx).inflate(R.layout.alert_dialog, null, false);
        TextView text = (TextView)alertView.findViewById(R.id.alert_text);
        text.setText(textId);

        Dialog diag = new Dialog(ctx);
        diag.requestWindowFeature(Window.FEATURE_NO_TITLE);
        diag.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        diag.setContentView(alertView);
        diag.setCancelable(false);

        return diag;
    }

    /**
     * set our screen to IMMMERRRRSSIIVEEEEE MODDDDEEEEEEE
     * @param activity
     */
    public static void goFullscreen(Activity activity)
    {
        activity.getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    /**
     * get Intent for sharing our awesome lip video
     * @param file file to share
     * @param caption optional caption
     * @return
     */
    public static Intent getShareIntent(File file, @Nullable String caption)
    {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("video/*");

        //add the file uri
        Uri uri = Uri.fromFile(file);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        //add caption if present
        if(caption != null)
            share.putExtra(Intent.EXTRA_TEXT, caption);

        return share;
    }

    /**
     * Task to delete a lot of files - hopefully this isn't even that necessary since it happens
     * so fast, but using it anyway since media scanner might take a while
     */
    public static class FileDeleteTask extends AsyncTask<Void, Void, String[]>
    {
        private final Activity activity;
        private final OnDeleteFilesCompleteListener completeListener;
        private final ArrayList<File> itemsToRemove;
        private final Dialog dialog;

        /**
         * A task that can delete a bunch of files, and needs an
         * activity that implements {@link .AndroidUtils.OnDeleteFilesCompleteListener}
         * @param activityListener
         * @param itemsToRemove
         */
        public FileDeleteTask(OnDeleteFilesCompleteListener activityListener, ArrayList<File> itemsToRemove)
        {
            if(!(activityListener instanceof Activity))
                throw new IllegalArgumentException("OnDeleteFilesCompleteListener cannot be anon - it must be attached to an Activity!");

            this.activity = (Activity)activityListener;
            this.completeListener = activityListener;
            this.itemsToRemove = itemsToRemove;

            //setup dialog
            int textId;
            if(itemsToRemove.size() > 1)
                textId = R.string.dialog_deleting_files;
            else
                textId = R.string.dialog_deleting_file;

            this.dialog = AndroidUtils.getAlertDialog(this.activity, textId);
        }

        @Override
        protected void onPreExecute() {
            this.dialog.show();
        }

        @Override
        protected String[] doInBackground(Void... params)
        {
            String[] filesToDelete = new String[itemsToRemove.size()];

            for(int i = 0; i < itemsToRemove.size(); i++)
            {
                File file = itemsToRemove.get(i);
                boolean deleted = file.delete();

                if(deleted)
                    filesToDelete[i] = file.toString();
            }

            return filesToDelete;
        }

        @Override
        protected void onPostExecute(String[] files)
        {
            this.completeListener.onDeleteFilesComplete(files);
            this.dialog.dismiss();
        }
    }


    /**
     * rotate bitmap based on exif data
     * thank you http://stackoverflow.com/questions/20478765/how-to-get-the-correct-orientation-of-the-image-selected-from-the-default-image
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This Interface is used in the above Task, and CANNOT be created as anon inner class.
     * It MUST be implemented by an activity since it serves a dual purpose mainly because lazy.
     */
    public interface OnDeleteFilesCompleteListener {
        void onDeleteFilesComplete(String[] filesToDelete);
    }

}
