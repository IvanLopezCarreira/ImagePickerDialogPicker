package sw.common.imagepicker.ui;

import android.graphics.Bitmap;

/**
 * Created by ivancarreira on 31/03/15.
 */
public interface TakePhoto {
    public void onPhotoTaken(String filePath, Bitmap bitmap);
    public void onIOError();
    public void onUnmountedExternalStorage();
}
