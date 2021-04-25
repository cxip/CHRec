package co.cxip.chrec.api.methods;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.cxip.chrec.App;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UpdatePhoto extends ClubhouseAPIRequest<Bitmap>{

	private final Uri uri;
	private Bitmap resizedBitmap;

	public UpdatePhoto(Uri fileUri){
		super("POST", "update_photo", Bitmap.class);
		uri=fileUri;
	}

	@Override
	public void prepare() throws IOException{
		Bitmap orig;
		int orientation=0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			try(InputStream in=App.applicationContext.getContentResolver().openInputStream(uri)) {
				ExifInterface exif = new ExifInterface(in);
				orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			}
		}
		Matrix matrix = new Matrix();
		if (orientation == 6) {
			matrix.postRotate(90);
		} else if (orientation == 3) {
			matrix.postRotate(180);
		} else if (orientation == 8) {
			matrix.postRotate(270);
		}
		try(InputStream in=App.applicationContext.getContentResolver().openInputStream(uri)){
			Bitmap sourceBitmap=BitmapFactory.decodeStream(in);
			orig = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
		}
		int size=Math.min(512, Math.min(orig.getWidth(), orig.getHeight()));
		resizedBitmap=Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Rect srcRect;
		if(orig.getWidth()>orig.getHeight()){
			int x=(orig.getWidth()-orig.getHeight())/2;
			srcRect=new Rect(x, 0, x+orig.getHeight(), orig.getHeight());
		}else{
			int y=(orig.getHeight()-orig.getWidth())/2;
			srcRect=new Rect(0, y, orig.getWidth(), y+orig.getWidth());
		}
		new Canvas(resizedBitmap).drawBitmap(orig, srcRect, new Rect(0, 0, size, size), new Paint(Paint.FILTER_BITMAP_FLAG));
		File tmp=new File(App.applicationContext.getCacheDir(), "ava_tmp.jpg");
		try(FileOutputStream out=new FileOutputStream(tmp)){
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
		}
		fileToUpload=tmp;
		fileFieldName="file";
		fileMimeType="image/jpeg";
	}

	@Override
	public Bitmap parse(String resp) throws Exception{
		return resizedBitmap;
	}
}
