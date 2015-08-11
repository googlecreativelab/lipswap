package com.androidexperiments.lipflip.data;

import android.content.Context;
import android.os.Environment;

import com.androidexperiments.lipflip.ChooserActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * stuffs
 */
public class Constants
{
    private static File mStorageDir;

    /**
     * easy to keep files dir consistent throughout app, in case we need to update this to something
     * more specific
     * @param ctx context from where u are calling
     * @return File pointing to the dir where to store files
     */
    public static File getStorageDir(Context ctx)
    {
        if(mStorageDir == null) {
            mStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LipService" + File.separator);// ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        }

        return mStorageDir;
    }

    /**
     * simple method to get all mp4's we've previously saved
     * @param ctx the activity we're calling from
     * @return List<File> of all mp4 files in LipFlip dir
     */
    public static List<File> getAllLipFlipVideos(Context ctx)
    {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = getStorageDir(ctx).listFiles();

        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getAllLipFlipVideos(ctx));
                } else {
                    if (file.getName().endsWith(".mp4")) {
                        inFiles.add(file);
                    }
                }
            }

            //sort by date modified
            Collections.sort(inFiles, new LipFlipComparator());

            return inFiles;
        }
        else
            return null;
    }

    /**
     * Comparator used to order the lip flip files from latest to oldest in
     * our {@link ChooserActivity}
     */
    public static class LipFlipComparator implements Comparator<File>
    {

        /**
         * Compare its lastModified param to determine display order
         */
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.lastModified() > rhs.lastModified() ? -1 : 1;
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }

    }
}
