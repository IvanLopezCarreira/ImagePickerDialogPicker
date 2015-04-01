package sw.common.imagepicker.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import sw.common.imagepicker.R;
import sw.common.imagepicker.SDUtils;
import sw.common.imagepicker.filesRoutes.ImagePickerFilesRoutes;


public class MainActivity extends ActionBarActivity implements TakePhoto {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        //File file = getExternalFilesDir(null);
        File file = getExternalCacheDir();
        Log.d(TAG, "getExternalFilesDir: " + file);
        String filePath = file.getAbsolutePath() + File.separator + "image.jpg";
        Log.d(TAG, "filepath passed: " + filePath);
        pickerDialogFragment.setPhotoFile(filePath, new ImagePickerFilesRoutes(getApplicationContext()));
        pickerDialogFragment.setPhotoTakenInterface(this);
        pickerDialogFragment.show(getSupportFragmentManager(), "ImagePickerDialogFragment");
    }

    @Override
    public void onPhotoTaken(String filePath) {
        Log.d(TAG, "onPhotoTaken: " + filePath);
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
