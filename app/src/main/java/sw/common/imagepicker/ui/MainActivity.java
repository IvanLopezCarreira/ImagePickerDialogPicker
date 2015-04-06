package sw.common.imagepicker.ui;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import sw.common.imagepicker.R;
import sw.common.imagepicker.SDUtils;
import sw.common.imagepicker.filesRoutes.ImagePickerFilesRoutes;


public class MainActivity extends ActionBarActivity implements TakePhoto {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.activity_main_image_view);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchPicker(View v) {
        Log.d(TAG, "isExternalStorageWritable: " + SDUtils.isExternalStorageWritable());
        ImagePickerDialogFragment pickerDialogFragment = new ImagePickerDialogFragment();
        File outputFile = new ImagePickerFilesRoutes(getApplicationContext()).getImagePermanentFileOnPrivateAppExternalStorage(null, generateFileName());
        Log.d(TAG, "filepath passed: " + outputFile.getAbsolutePath());
        pickerDialogFragment.setPhotoFile(outputFile, new ImagePickerFilesRoutes(getApplicationContext()));
        pickerDialogFragment.setPhotoTakenInterface(this);
        pickerDialogFragment.show(getSupportFragmentManager(), "ImagePickerDialogFragment");
    }

    private String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        return imageFileName;
    }

    @Override
    public void onPhotoTaken(String filePath, Bitmap bitmap) {
        Log.d(TAG, "onPhotoTaken: " + filePath);

        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onIOError() {
        Log.d(TAG, "onIOError()");
    }

    @Override
    public void onUnmountedExternalStorage() {
        Log.d(TAG, "onUnmountedExternalStorage()");
    }
}
