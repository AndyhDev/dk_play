package com.dk.play.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.dk.play.database.SQLRemoved;
import com.dk.play.database.SQLRemovedList;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;

public class SearchPlayable {
	private static final String TAG = "SearchPlayable";
	private Context context;
	private SQLiteDataSource datasource;
	private SQLRemovedList removedList;
	private SQLSongList songList;
	private SearchListener listener;
	private Handler handler = new Handler();
	private List<String> extensions = new ArrayList<String>();
	
	public SearchPlayable(Context context) {
		this.context = context;
		extensions.add("mp3");
		extensions.add("mp4");
		extensions.add("ogg");
	}

	public void setSearchListener(SearchListener listener){
		this.listener = listener;
	}
	public void addSingelPath(String path, Boolean deleteRemoved){
		//TODO check path exists
		datasource = new SQLiteDataSource(context);
		datasource.open();
		if(deleteRemoved){
			SQLRemovedList rmList = datasource.getSQLRemovedList();
			if(rmList.isIn(path)){
				SQLRemoved item = rmList.get(path);
				datasource.removeRemoved(item);
			}
		}
		addPath(path);
		datasource.close();
	}
	public void addSingelPath(String path){
		//TODO check path exists
		datasource = new SQLiteDataSource(context);
		datasource.open();
		addPath(path);
		datasource.close();
	}
	public void search(){
		new Thread(new Runnable() {
			public void run() {
				if(listener != null){
					handler.post(new Runnable(){
						@Override
						public void run() {
							listener.onStart();
						}
					});
				}

				doSearch();

				if(listener != null){
					handler.post(new Runnable(){
						@Override
						public void run() {
							listener.onEnd();
						}
					});
				}
			}
		}).start();
	}
	private void doSearch(){
		datasource = new SQLiteDataSource(context);
		datasource.open();

		removedList = datasource.getSQLRemovedList();
		songList = datasource.getSQLSongList();

		String selection_music = MediaStore.Audio.Media.IS_MUSIC + " = 1";
		String selection_video = MediaStore.Video.Media._ID;
		final String[] projection_music = new String[] {MediaStore.Audio.Media.DATA};
		final String[] projection_video = new String[] {MediaStore.Video.Media.DATA};

		Uri audioEx = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Uri videoEx = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		
		Boolean searchOk = true;
		Cursor cursor1 = context.getContentResolver().query(audioEx, projection_music, selection_music, null, null);
		processCursor(cursor1);
		if(cursor1 != null){
			if(cursor1.getCount() == 0){
				searchOk = false;
			}
			cursor1.close();
		}else{
			searchOk = false;
		}

		Cursor cursor2 = context.getContentResolver().query(videoEx, projection_video, selection_video, null, null);
		processCursor(cursor2);
		if(cursor2 != null){
			if(cursor2.getCount() == 0){
				searchOk = false;
			}
			cursor2.close();
		}else{
			searchOk = false;
		}
		if(!searchOk){
			Log.d(TAG, "GUT");
			processDirectory(Environment.getExternalStorageDirectory());
		}
		datasource.close();

	}
	private void processCursor(Cursor cursor){
		if(cursor == null){
			return;
		}
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			String path = cursor.getString(0);
			if(!removedList.isIn(path) && !songList.isIn(path)){
				addPath(path);
			}
			cursor.moveToNext();
		}
	}
	private void processDirectory(File dir){
		File[] files = dir.listFiles();
		if(files != null){
			for(int i = 0; i < files.length; i++){
				File f = files[i];
				if(f.isDirectory()){
					processDirectory(f);
				}else if(f.isFile()){
					processFile(f);
				}
			}
		}

	}
	private void processFile(File f){
		String ext = getExtension(f);
		Log.d(TAG, "FILE=" + f.getAbsolutePath());
		if(extensions.indexOf(ext) != -1){
			String path = f.getAbsolutePath();
			if(!removedList.isIn(path) && !songList.isIn(path)){
				addPath(path);
			}
		}
	}
	private String getExtension(File f){
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    return fileName.substring(i+1);
		}
		return null;
	}
	private void addPath(String path){
		Log.d(TAG, "addPath('" + path + "')");
		String artist = "";
		String album = "";
		String title = "";
		String genre = "";
		byte[] cover = null;
		int type = SQLSong.TYPE_MUSIC;

		MediaMetadataRetriever id3 = new MediaMetadataRetriever();
		try{
			id3.setDataSource(path);

			title = id3.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			if(title == null){
				title = path.substring(path.lastIndexOf(File.separator) + 1);
				if (title.indexOf(".") > 0) {
					title = title.substring(0, title.lastIndexOf("."));
				}
			}
			album = id3.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			if(album == null){
				album = "";
			}
			artist = id3.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			if(artist == null){
				artist = "";
			}
			genre = id3.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
			if(genre == null){
				genre = "";
			}
			cover = id3.getEmbeddedPicture();
			if(cover == null){
				Bitmap bmp = id3.getFrameAtTime();
				type = SQLSong.TYPE_VIDEO;
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
			title = path.substring(path.lastIndexOf(File.separator) + 1);
			if (title.indexOf(".") > 0) {
				title = title.substring(0, title.lastIndexOf("."));
			}
			cover = null;
		}finally{
			id3.release();
			String coverName;
			if(cover == null){
				coverName = "no";
			}else{
				coverName = Long.toString(System.currentTimeMillis()).replace(".", "");
				saveCover(cover, coverName + ".jpg");
			}
			File song = new File(path);
			long date = song.lastModified();
			final SQLSong sqlSong = new SQLSong(0, title, artist, album, genre, coverName, 0, 0, path, date, type, 0);
			datasource.addSong(sqlSong);
			if(listener != null){
				handler.post(new Runnable(){
					@Override
					public void run() {
						listener.onFound(sqlSong);
					}
				});
			}
		}
	}
	private void saveCover(byte[] cover, String fileName){
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
