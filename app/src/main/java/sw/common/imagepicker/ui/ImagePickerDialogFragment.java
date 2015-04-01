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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import sw.common.imagepicker.domain.ProcessImage;
import sw.common.imagepicker.domain.ProcessImageInteractorFactory;
import sw.common.imagepicker.executor.Executor;
import sw.common.imagepicker.executor.MainThread;
import sw.common.imagepicker.filesRoutes.ImagePickerFilesRoutes;

public class ImagePickerDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = ImagePickerDialogFragment.class.getSimpleName();
	
	public final static int REQUEST_IMAGE_CAPTURE = 15;
	public final static int REQUEST_IMAGE_GALLERY = REQUEST_IMAGE_CAPTURE + 1;

    private final static String KEY_OUTPUT_FILE = "key_output_file";
    private final static String KEY_CACHE_PHOTO_FILE = "key_cache_photo_file";

    private LinearLayout selectButtonsLayout;
    private RelativeLayout processImageLayout;

    private ImagePickerFilesRoutes mImagePickerFilesRoutes;

	private File cachePhotoFile;//This is route to image on externalCacheDir where camera save image
    private String outputFilePath;//This is route to image on dependant app permanent directory where processed image will save
    private TakePhoto mCallback;

    private ProcessImage processImage;


    //To return result to caller
    public void setPhotoTakenInterface(TakePhoto photoTakenInterface){
        this.mCallback = photoTakenInterface;
    }

    /**
     * This method must be called before .show()
     * @param outputFilePath
     * @param imagePickerFilesRoutes
     */

    public void setPhotoFile(String outputFilePath, ImagePickerFilesRoutes imagePickerFilesRoutes) {
        this.mImagePickerFilesRoutes = imagePickerFilesRoutes;
        this.outputFilePath = outputFilePath;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            outputFilePath = savedInstanceState.getString(KEY_OUTPUT_FILE);
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
        outState.putString(KEY_OUTPUT_FILE, outputFilePath);

        super.onSaveInstanceState(outState);
    }

    @Override
	public void onClick(View v) {
        if (BuildConfig.DEBUG){
            Log.e(TAG, "onClick");
        }
        if (mImagePickerFilesRoutes == null || mCallback == null || outputFilePath == null) {
            Log.e(TAG, "this Dialog needs you calls \"setPhotoFile(String outputFilePath, ImagePickerFilesRoutes imagePickerFilesRoutes)\" and \"setPhotoTakenInterface(TakePhoto photoTakenInterface)\"");
            return;
        }
		switch (v.getId()) {
		case R.id.fd_camera:
            if (BuildConfig.DEBUG){
                Log.e(TAG, "fd_camera");
            }
            if (createCacheFileOnPublicDirectory()) {
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

    private boolean createCacheFileOnPublicDirectory() {
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
                mCallback.onIOError();
                dismiss();
                return false;
            }
            return fileCreated;
        } else {
            mCallback.onUnmountedExternalStorage();
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
        setCancelable(false);
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
                processImage();

                //moveFileFromToSelectedDir(cachePhotoFile, outputFilePath);

                //testImage();
                //onPhotoTaken(outputFilePath);

                } else if (requestCode == ImagePickerDialogFragment.REQUEST_IMAGE_GALLERY) {
                /*Log.e(TAG, "request image gallery");
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                String savedFotoName ="temp.jpeg";
                filePath = SDUtils.moveFileToAppCache(getActivity(), filePath, savedFotoName);

                //userPhotoPath = filePath;
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
                }
                onPhotoTaken(bitmap);*/
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "code not implemented");
                }
            }
        }else{
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "canceled");
            }
        }
    }

    //used to rotate image if ExifInterface is not NORMAL
    private void processImage() {
        Log.d(TAG, "processImage()");

        ProcessImageInteractorFactory processImageInteractorFactory = new ProcessImageInteractorFactory();
        processImage = processImageInteractorFactory.getProcessImage();

        processImage.execute(cachePhotoFile, new File(outputFilePath), new ProcessImage.Callback() {
            @Override
            public void onImageProcessed(String outputPath, Bitmap bitmap) {
                Log.d(TAG, "onImageProcessed CALLBACK");
                dismiss();
            }

            @Override
            public void onUnmountedExternalStorage() {
                Log.d(TAG, "onUnmountedExternalStorage CALLBACK");
                dismiss();
            }

            @Override
            public void onIOError() {
                Log.d(TAG, "onIOError CALLBACK");
                dismiss();
            }
        });

        /*Bitmap mPhoto = BitmapFactory.decodeFile(cachePhotoFile);

        if (mPhoto == null) {
            Log.e(TAG, "bitmap to process is null");
        } else {
            Log.d(TAG, "Image bitmap to proccess bytes: " + mPhoto.getByteCount());
        }

        //put image on view
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(cachePhotoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exifInterface != null) {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d(TAG, "orientation: " + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    Log.d(TAG, "orientation_normal");
                    //nothing to do
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    Log.d(TAG, "orientation_flip_horizontal");
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    Log.d(TAG, "orientation_flip_vertical");
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.d(TAG, "orientation_rotate_90");
                    rotateImage(mPhoto, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.d(TAG, "orientation_rotate_180");
                    rotateImage(mPhoto, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.d(TAG, "orientation_rotate_270");
                    rotateImage(mPhoto, 270);
                    break;
            }
        }*/
    }

    private Bitmap rotateImage(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
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
        File file = new File(outputFilePath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            Log.d(TAG, "output bitmap is null");
        } else {
            Log.d(TAG, "bitmap is correct, size: " + bitmap.getByteCount());
        }
    }

    private void moveFileFromToSelectedDir(String oldFilePath, String newFilePath) {
        File oldFile = new File(cachePhotoFile.getAbsolutePath());
        File newFile = new File(outputFilePath);

        try {
            InputStream in = null;
            OutputStream out = null;

            in = new FileInputStream(oldFile.getAbsolutePath());
            boolean createNewFile = newFile.createNewFile();
            Log.d(TAG, "new file created: " + createNewFile);

            out = new FileOutputStream(newFile);
            copyFile(in, out);

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
            Log.d(TAG, "File moved: " + oldFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
        } catch (IOException e) {
            mCallback.onIOError();
            e.printStackTrace();
            Log.d(TAG, "File didnt move");
        }
    }


    public void onPhotoTaken(String outputFilePath){
        if (mCallback != null){
            mCallback.onPhotoTaken(outputFilePath);
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

    public static String saveBitmapToExternalAppCache(Context context, Bitmap bitmap, String fileName){
/*        String filePath = new String(getExternalCacheExtorage(context).getAbsolutePath() + File.separatorChar + fileName);
        File file = new File(filePath);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                out.close();
            } catch(Throwable ignore) {}
        }
        return "";*/
        return null;
    }

    public static String moveFileToAppCache(Context context,String filePath, String fileName) {
        //		File file = new File(filePath + fileName);
        //		moved = file.renameTo(new File(SDUtils.getExternalCacheExtorage(context) + fileName));
        //
        //		return moved;
/*        if (context == null || filePath == null || fileName == null || !existFile(filePath)) {
            return "";
        }*/
        File oldFile = new File(filePath);
        File newFile = new File(SDUtils.getExternalCacheExtorage(context) + File.separator + fileName);
        try {
            InputStream in = null;
            OutputStream out = null;

            in = new FileInputStream(oldFile.getAbsolutePath());
            newFile.createNewFile();

            out = new FileOutputStream(newFile);
            copyFile(in, out);

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
            Log.d(TAG, "File moved: " + filePath + " to " + SDUtils.getExternalCacheExtorage(context) + File.separator + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "File didnt move");
        }
        return newFile.getAbsolutePath();
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];

        int read;

        while((read = in.read(buffer)) != -1){

            out.write(buffer, 0, read);

        }
    }
}
