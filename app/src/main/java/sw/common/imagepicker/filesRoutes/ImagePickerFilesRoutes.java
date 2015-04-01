package sw.common.imagepicker.filesRoutes;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ImagePickerFilesRoutes extends BaseFilesRoutes{

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

        return imageFile;
    }
}
