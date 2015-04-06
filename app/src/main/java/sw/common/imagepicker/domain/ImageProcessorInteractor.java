package sw.common.imagepicker.domain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sw.common.imagepicker.executor.Executor;
import sw.common.imagepicker.executor.Interactor;
import sw.common.imagepicker.executor.MainThread;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ImageProcessorInteractor implements Interactor, ImageProcessor {
    private static final String TAG = ImageProcessorInteractor.class.getSimpleName();

    private static final int COMPRESS_QUALITY = 80;

    private final Executor executor;
    private final MainThread mainThread;

    private File inputFile;
    private File outputFile;
    private Callback callback;

    public ImageProcessorInteractor(Executor executor, MainThread mainThread) {
        this.executor = executor;
        this.mainThread = mainThread;
    }


    @Override
    public void execute(File inputPath, File outputPath, int sampleSizeToOptions, Callback callback) {
        this.inputFile = inputPath;
        this.outputFile = outputPath;
        this.callback = callback;

        this.executor.run(this);
    }

    @Override
    public void run() {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        Bitmap processedBitmap = processImage();
        if (processedBitmap != null) {
            saveFileOnOutputPath(processedBitmap);
        } else {
            onIOError();
        }

        onImageProcessed(outputFile.getAbsolutePath(), processedBitmap);
    }

    private Bitmap processImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap originalImage = BitmapFactory.decodeFile(inputFile.getAbsolutePath(), options);

        if (originalImage == null) {
            Log.e(TAG, "bitmap to process is null");
            onIOError();
            return null;
        } else {
            Log.d(TAG, "Image bitmap to proccess bytes: " + originalImage.getByteCount());
        }

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(inputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            onIOError();
            return null;
        }

        Bitmap processedImageBitmap = null;

        if (exifInterface != null) {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d(TAG, "orientation: " + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    Log.d(TAG, "orientation_normal");
                    //nothing to do
                    processedImageBitmap = originalImage;
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    Log.d(TAG, "orientation_flip_horizontal");
                    Log.e(TAG, "should never occurs!! This case is not covered");
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    Log.d(TAG, "orientation_flip_vertical");
                    Log.e(TAG, "should never occurs!! This case is not covered");
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.d(TAG, "orientation_rotate_90");
                    processedImageBitmap = rotateImage(originalImage, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.d(TAG, "orientation_rotate_180");
                    processedImageBitmap = rotateImage(originalImage, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.d(TAG, "orientation_rotate_270");
                    processedImageBitmap = rotateImage(originalImage, 270);
                    break;
                default:
                    Log.d(TAG, "orientation not covered, orientation = " + orientation);
                    processedImageBitmap = originalImage;
            }
        }

        return processedImageBitmap;
    }

    private Bitmap rotateImage(Bitmap imageBitmap, int rotationAngle) {
        Log.d(TAG, "rotateImage");
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle, imageBitmap.getWidth()/2, imageBitmap.getHeight()/2);

        Bitmap rotated = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

        return rotated;
    }

    private void saveFileOnOutputPath(Bitmap processedImageBitmap) {
        if (outputFile.exists()) {
            Log.d(TAG, " File exists and its size is: " + outputFile.length());
        } else {
            try {
                boolean fileCreated = outputFile.createNewFile();
                Log.d(TAG, "fileCreated: " + fileCreated);
            } catch (IOException e) {
                Log.e(TAG, "Probably the directory does not exist");
                onIOError();
                e.printStackTrace();
            }
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            boolean compressed = processedImageBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream);
            Log.d(TAG, "file compressed and saved: " + compressed);
            outputStream.flush();

        } catch (FileNotFoundException e) {
            onIOError();
            e.printStackTrace();
        } catch (IOException e) {
            onIOError();
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onImageProcessed(final String outputPath, final Bitmap bitmap) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onImageProcessed(outputPath, bitmap);
                }
            }
        });
    }

    private void onIOError() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onIOError();
                }
            }
        });
    }

/*    private void onUnmountedExternalStorage() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onUnmountedExternalStorage();
                }
            }
        });
    }*/
}
