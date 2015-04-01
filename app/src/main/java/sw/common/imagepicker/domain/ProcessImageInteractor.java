package sw.common.imagepicker.domain;

import android.graphics.Bitmap;

import java.io.File;

import sw.common.imagepicker.executor.Executor;
import sw.common.imagepicker.executor.Interactor;
import sw.common.imagepicker.executor.MainThread;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ProcessImageInteractor implements Interactor, ProcessImage {
    private final Executor executor;
    private final MainThread mainThread;

    private File inputPath;
    private File outputPath;
    private Callback callback;

    public ProcessImageInteractor(Executor executor, MainThread mainThread) {
        this.executor = executor;
        this.mainThread = mainThread;
    }


    @Override
    public void execute(File inputPath, File outputPath, Callback callback) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.callback = callback;

        this.executor.run(this);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            onIOError();
        }
        long time = System.currentTimeMillis();
        int timeInt = (int)time;
        if (timeInt % 2 == 0) {
            onImageProcessed(null, null);
        } else {
            onUnmountedExternalStorage();
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

    private void onUnmountedExternalStorage() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onUnmountedExternalStorage();
                }
            }
        });
    }
}
