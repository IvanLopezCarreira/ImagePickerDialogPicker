package sw.common.imagepicker.filesRoutes;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class BaseFilesRoutes {
    private static final String TAG = "BaseFilesRoutes";

    private Context context;

    public BaseFilesRoutes(Context applicationContext) {
        this.context = applicationContext;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPrivateInternalStorageDir() {
        File directory = context.getFilesDir();
        if (!directory.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return directory;
    }

    public File getPublicExternalDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    public File getExternalCacheDir() {
        return context.getExternalCacheDir();
    }

    public File getApplicationExternalDirectory() {
        File file = new File(context.getExternalFilesDir(null).getAbsolutePath());
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public File getApplicationExternalTypeSubdirectory(String type) {
        File file = new File(context.getExternalFilesDir(type).getAbsolutePath());
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public File getApplicationExternalSubdirectories(String type, String subSubDirectoryName) {
        File file = new File(context.getExternalFilesDir(type), subSubDirectoryName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }


}
