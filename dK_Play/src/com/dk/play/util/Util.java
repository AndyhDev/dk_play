package com.dk.play.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.dk.play.App;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;

public class Util {
	public static void removeSQLSong(SQLSong song){
		File cover = new File(song.getCoverUri().getPath());
		cover.delete();
		
		SQLiteDataSource datasource = new SQLiteDataSource(App.getContextStatic());
		datasource.open();
		datasource.removeSQLSong(song);
		datasource.close();
	}
	public static String genCover(SQLSong song){
		return genCover(song.getPath());
	}
	public static String genCover(String path){
		byte[] cover = null;
		String coverName;
		
		MediaMetadataRetriever id3 = new MediaMetadataRetriever();
		try{
			id3.setDataSource(path);
			cover = id3.getEmbeddedPicture();
			if(cover == null){
				Bitmap bmp = id3.getFrameAtTime();
				int height = bmp.getHeight();
				int width = bmp.getWidth();
				int size = 0;
				int x = 0;
				int y = 0;
				if(height > width){
					size = width;
					y = (height - width) / 2; 
				}else{
					size = height;
					x = (width - height) / 2;
				}
				Bitmap bmp2 = Bitmap.createBitmap(bmp, x, y, size, size);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp2.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				cover = stream.toByteArray();
			}
		}catch(RuntimeException ex){
			ex.printStackTrace();
			cover = null;
		}finally{
			id3.release();
			if(cover == null){
				coverName = "no";
			}else{
				coverName = Long.toString(System.currentTimeMillis()).replace(".", "");
				saveCover(cover, coverName + ".jpg");
			}
		}
		return coverName;
	}
	
	private static void saveCover(byte[] cover, String fileName){
		File file = new File(Paths.getCoverDir(), fileName);

		try {
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			fOut.write(cover);
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
