package sw.common.imagepicker.domain;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by ivancarreira on 01/04/15.
 */
public interface ProcessImage {

    interface Callback {
        void onImageProcessed(String outputPath, Bitmap bitmap);
        void onUnmountedExternalStorage();
        void onIOError();
    }

    void execute(final File inputPath,final File outputPath, final Callback callback);
}
