package sw.common.imagepicker.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import sw.common.imagepicker.BuildConfig;
import sw.common.imagepicker.R;
import sw.common.imagepicker.SDUtils;
import sw.common.imagepicker.domain.ImageProcessor;
import sw.common.imagepicker.domain.ImageProcessorInteractorFactory;
import sw.common.imagepicker.filesRoutes.ImagePickerFilesRoutes;

public class ImagePickerDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = ImagePickerDialogFragment.class.getSimpleName();
	
	public final static int REQUEST_IMAGE_CAPTURE = 15;
	public final static int REQUEST_IMAGE_GALLERY = REQUEST_IMAGE_CAPTURE + 1;

    private final static String KEY_OUTPUT_FILE = "key_output_file";
    private final static String KEY_CACHE_PHOTO_FILE = "key_cache_photo_file";

    private final static int DEFAULT_SAMPLE_SIZE_OPTIONS = 2;

    private LinearLayout selectButtonsLayout;
    private RelativeLayout processImageLayout;

    private ImagePickerFilesRoutes mImagePickerFilesRoutes;

	private File cachePhotoFile;//This is route to image on externalCacheDir where camera save image
    private File outputFile;//This is route to image on dependant app permanent directory where processed image will save
    private TakePhoto mCallback;

    private ImageProcessor imageProcessor;


    //To return result to caller
    public void setPhotoTakenInterface(TakePhoto photoTakenInterface) {
        this.mCallback = photoTakenInterface;
    }

    /**
     * This method must be called before .show()
     * @param outputFile
     * @param imagePickerFilesRoutes
     */

    public void setPhotoFile(File outputFile, ImagePickerFilesRoutes imagePickerFilesRoutes) {
        this.mImagePickerFilesRoutes = imagePickerFilesRoutes;
        this.outputFile = outputFile;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            outputFile = (File)savedInstanceState.getSerializable(KEY_OUTPUT_FILE);
            cachePhotoFile = (File)savedInstanceState.getSerializable(KEY_CACHE_PHOTO_FILE);
        }

		int style = DialogFragment.STYLE_NO_TITLE, theme = android.R.style.Theme_Translucent_NoTitleBar;
		setStyle(style, theme);
        this.setCancelable(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog_imagepicker, container, false);

        bindViews(view);
		Button cameraBTN = (Button) view.findViewById(R.id.fd_camera);
		Button galleryBTN = (Button) view.findViewById(R.id.fd_gallery);

		cameraBTN.setOnClickListener(this);
		galleryBTN.setOnClickListener(this);

		return view;
	}

    private void bindViews(View view) {
        selectButtonsLayout = (LinearLayout) view.findViewById(R.id.fragment_dialog_imagepicker_select_buttons_layout);
        processImageLayout = (RelativeLayout) view.findViewById(R.id.fragment_dialog_imagepicker_process_layout);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_CACHE_PHOTO_FILE, cachePhotoFile);
        outState.putSerializable(KEY_OUTPUT_FILE, outputFile);

        super.onSaveInstanceState(outState);
    }

    @Override
	public void onClick(View v) {
        if (BuildConfig.DEBUG){
            Log.e(TAG, "onClick");
        }
        if (mImagePickerFilesRoutes == null || mCallback == null || outputFile == null) {
            Log.e(TAG, "this Dialog needs you calls \"setPhotoFile(String outputFilePath, ImagePickerFilesRoutes imagePickerFilesRoutes)\" and \"setPhotoTakenInterface(TakePhoto photoTakenInterface)\"");
            return;
        }
		switch (v.getId()) {
		case R.id.fd_camera:
            if (BuildConfig.DEBUG){
                Log.e(TAG, "fd_camera");
            }
            if (createCacheFileOnExternalCacheDirectory()) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (cachePhotoFile != null) {
                    if (BuildConfig.DEBUG){
                        Log.d(TAG, "take picture --> putExtra");
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cachePhotoFile));
                }
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
			break;
		case R.id.fd_gallery:
			Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, REQUEST_IMAGE_GALLERY);
			break;
		}
	}

    private boolean createCacheFileOnExternalCacheDirectory() {
        boolean fileCreated = false;

        if (mImagePickerFilesRoutes.isExternalStorageReadable() && mImagePickerFilesRoutes.isExternalStorageWritable()) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + ".jpg";
            try {
                cachePhotoFile = mImagePickerFilesRoutes.getImageFileOnExternalCacheDir(File.separator + imageFileName);
                Log.d(TAG, "cachePhotoFile: " + cachePhotoFile.getAbsolutePath());
                if (!cachePhotoFile.exists()) {
                    fileCreated = cachePhotoFile.createNewFile();
                } else {
                    Log.e(TAG, "file already exists!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                onIOError();
                dismiss();
                return false;
            }
            return fileCreated;
        } else {
            onUnmountedExternalStorage();
            dismiss();
            return false;
        }
    }

    private void setDialogNotCancelable() {
        this.setCancelable(false);
    }

    private void changeToProcessLayout() {
        selectButtonsLayout.setVisibility(View.GONE);
        processImageLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (BuildConfig.DEBUG){
            Log.e(TAG, "onActivityResult, requestCOde: " + requestCode + " resultCode: " + resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (BuildConfig.DEBUG){
                Log.e(TAG, "RESULT_OK");
            }
            if (requestCode == ImagePickerDialogFragment.REQUEST_IMAGE_CAPTURE) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "REQUEST_IMAGE_CAPTURE");
                }

                testImageOnCache();
                changeToProcessLayout();
                setDialogNotCancelable();

                processImage(null); // In background

            } else if (requestCode == ImagePickerDialogFragment.REQUEST_IMAGE_GALLERY) {
                Log.e(TAG, "request image gallery");
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                processImage(filePath);

                /*//Comment this
                Bitmap bitmap = null;
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    bitmap = BitmapFactory.decodeFile(filePath, options);

                    if (bitmap != null){
                        Log.e(TAG, "photo bitmap is not null");
                    }
                } catch (OutOfMemoryError e){
                    Log.d(TAG, "Out of memory" + e.getStackTrace().toString());
                }*/
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "code not implemented");
                }
            }
        }else {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "canceled");
            }
            dismiss();
        }
    }

    //used to rotate image if ExifInterface is not NORMAL
    private void processImage(String filePath) {
        Log.d(TAG, "processImage()");

        ImageProcessorInteractorFactory imageProcessorInteractorFactory = new ImageProcessorInteractorFactory();
        imageProcessor = imageProcessorInteractorFactory.getProcessImage();

        ImageProcessor.Callback callback = new ImageProcessor.Callback() {
            @Override
            public void onImageProcessed(String outputPath, Bitmap bitmap) {
                Log.d(TAG, "onImageProcessed CALLBACK");
                if (bitmap != null) {
                    Log.d(TAG, "returned bitmap size: " + bitmap.getByteCount() + " bytes");
                } else {
                    Log.e(TAG, "returned bitmap is null");
                }

                Bitmap bitmapFromFile = BitmapFactory.decodeFile(outputPath);
                if (bitmapFromFile != null) {
                    Log.d(TAG, "returned bitmap size from output file: " + bitmap.getByteCount() + " bytes");
                } else {
                    Log.e(TAG, "returned bitmap from output file is null");
                }

                onPhotoTaken(outputPath, bitmap);

                dismiss();
            }

            @Override
            public void onIOError() {
                Log.d(TAG, "onIOError CALLBACK");
                dismiss();
            }
        };

        if (filePath == null) {
            imageProcessor.execute(cachePhotoFile, outputFile, DEFAULT_SAMPLE_SIZE_OPTIONS, callback);
        } else {
            imageProcessor.execute(new File(filePath), outputFile, DEFAULT_SAMPLE_SIZE_OPTIONS, callback);
        }
    }

    private void testImageOnCache() {
        File file = new File(cachePhotoFile.getAbsolutePath());
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            Log.d(TAG, "output bitmap in cache is null");
        } else {
            Log.d(TAG, "bitmap in cache is correct, size: " + bitmap.getByteCount());
        }
    }

    private void testImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
        if (bitmap == null) {
            Log.d(TAG, "output bitmap is null");
        } else {
            Log.d(TAG, "bitmap is correct, size: " + bitmap.getByteCount());
        }
    }


    private void onPhotoTaken(String outputFilePath, Bitmap bitmap){
        if (mCallback != null){
            mCallback.onPhotoTaken(outputFilePath, bitmap);
        }
    }

    private void onIOError() {
        if (mCallback != null) {
            mCallback.onIOError();
        }
    }

    private void onUnmountedExternalStorage() {
        if (mCallback != null) {
            mCallback.onUnmountedExternalStorage();
        }
    }


    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
