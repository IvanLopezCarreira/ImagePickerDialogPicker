package sw.common.imagepicker.filesRoutes;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ImagePickerFilesRoutes extends BaseFilesRoutes{
    private static final String TAG = ImagePickerFilesRoutes.class.getSimpleName();

    public ImagePickerFilesRoutes(Context applicationContext) {
        super(applicationContext);
    }

    public File getImageFileOnExternalCacheDir(String fileName) {
        File routeDirectory = getExternalCacheDir();

        File imageFile = new File(routeDirectory + File.separator + fileName);

        return imageFile;
    }

    public File getImagePermanentFileOnPrivateAppExternalStorage(String type, String fileName) {
        File routeDirectory = getApplicationExternalTypeSubdirectory(type);

        File imageFile = new File(routeDirectory + File.separator + fileName);

        if (imageFile.exists()) {
            Log.d(TAG, "file already exists, deleting...");
            imageFile.delete();
        }
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "error creating file");
            e.printStackTrace();
        }

        return imageFile;
    }
}
