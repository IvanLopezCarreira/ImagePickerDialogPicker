package sw.common.imagepicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class SDUtils {
	private static final String TAG = "SDUtils";
	
	/**
	 *  Checks if external storage is available for read and write 
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 *  Checks if external storage is available to at least read 
	 */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
	
	public static File getPrivateDownloadsStorageDir(Context context) {
		if (BuildConfig.DEBUG){
			Log.e(TAG, "directory to create: " + context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath());
		}
		File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		if (file.exists()){
			if (BuildConfig.DEBUG){
				Log.e(TAG, "Directory exists: " + file.getPath());
			}
			return file;
		}
		if (!file.mkdirs()) {
			if (BuildConfig.DEBUG){
				Log.e(TAG, "Directory not created");
			}
		}else{
			if (BuildConfig.DEBUG){
				Log.e(TAG, "Created succesfully: " + file.getPath());
			}
		}
		return file;
	}

	public static boolean isDirEmpty(File dir){
		if (dir == null)
			return true;

		File[] dirFiles = dir.listFiles();

		if (dirFiles == null){
			return true;
		}

		return false;
	}
	
	public static boolean existFile(String path, String fileName){
		if (path == null || fileName == null){
			return false;
		}
		File file = new File(path + File.separatorChar + fileName);
		if (file.exists()){
			return true;
		}
		return false;
	}
	
	public static boolean existFile(String filePath){
		if (filePath == null){
			return false;
		}
		File file = new File(filePath);
		if (file.exists()){
			return true;
		}
		return false;
	}
	
	public static Bitmap getBitmapInExternalStorage(String path, String fileName){
		File imgFile = new File(path + File.separatorChar + fileName);
		
		if (BuildConfig.DEBUG){
			Log.e(TAG, "Image path: " + imgFile.getPath());
		}
		
		Bitmap bitmap = null;
		if(imgFile.exists()){
			if (BuildConfig.DEBUG){
				Log.e(TAG, "Image exists, decoding...");
			}
			bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			if (BuildConfig.DEBUG){
				Log.e(TAG, (bitmap!=null?"bitmap succesfull decoded":"bitmap is null"));
			}
		}
		return bitmap;
	}
	
	public static File getExternalCacheExtorage(Context context){
		return context.getExternalCacheDir();
	}

    public static File getCacheExtorage(Context context){
        return context.getCacheDir();
    }

    public static String saveBitmapToInternalAppCache(Context context, Bitmap bitmap, String fileName){
        String filePath = new String(getCacheExtorage(context).getAbsolutePath() + File.separatorChar + fileName);
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
        return "";
    }

    public static String saveBitmapToExternalAppCache(Context context, Bitmap bitmap, String fileName){
		String filePath = new String(getExternalCacheExtorage(context).getAbsolutePath() + File.separatorChar + fileName);
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
		return "";
	}
	
	public static String getFileName(String absoluteFilePath){
		if (absoluteFilePath == null){
			return "";
		}
		
		String fileParts[] = absoluteFilePath.split(File.separator);
		return fileParts[fileParts.length - 1];
	}
	
	public static boolean deleteFileInAppCache(Context context, String fileName) {
		boolean deleted = false;
		if (fileName == null || context == null){
			return deleted;
		}
		String filePath = new String(getExternalCacheExtorage(context).getAbsolutePath() + File.separatorChar + fileName);
		File file = new File(filePath);
		deleted = file.delete();
		
		return deleted;
	}
	
	public static String moveFileToAppCache(Context context,String filePath, String fileName) {
		//		File file = new File(filePath + fileName);
		//		moved = file.renameTo(new File(SDUtils.getExternalCacheExtorage(context) + fileName));
		//
		//		return moved;
		if (context == null || filePath == null || fileName == null || !existFile(filePath)) {
			return "";
		}		
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
