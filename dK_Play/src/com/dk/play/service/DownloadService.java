package com.dk.play.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.adv.AdvSong;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.util.Paths;
import com.dk.util.network.ApiDownloadListener;
import com.dk.util.network.dKApiDownload;
import com.dk.util.network.dKSession;

public class DownloadService extends Service{
	private static final String TAG = "DownloadService";
	
	private final IBinder mBinder = new MyBinder();
	private boolean started = false;
	private Builder builder;
	private static final int notifyId = 25;
	private AdvControl adv;
	private dKSession session;
	
	private int BUFFER_SIZE = 1024;
	private byte[] buffer = new byte[BUFFER_SIZE];
	
	public static final String KEY_ID = "KEY_ID";
	private ArrayList<String> queue = new ArrayList<String>();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		adv = new AdvControl(this);
		
		if(intent != null){
			Log.d(TAG, "1");
			String id = intent.getStringExtra(KEY_ID);
			if(id != null){
				Log.d(TAG, "2");
				if(!queue.contains(id)){
					Log.d(TAG, "3");
					queue.add(id);
				}
			}
		}
		if(queue.size() != 0){
			Log.d(TAG, "4");
			startQueue();
		}
		return Service.START_NOT_STICKY;
	}
	private void startQueue(){		
		if(started){
			return;
		}
		Log.d(TAG, "5");
		builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getText(R.string.adv))
		.setContentText("lade Song herunter...")
		.setSmallIcon(R.drawable.ic_download);
		builder.setProgress(100, 0, true);
		
		startForeground(notifyId, builder.build());
		started = true;
		
		Log.d(TAG, "6");
		Thread thread = new Thread(){
			@Override
			public void run(){
				session = adv.getSession();
				if(session == null){
					stopForeground(true);
					return;
				}
				for(int i = 0; i < queue.size(); i++){
					download(queue.get(i));
				}
				started = false;
				queue.clear();
				stopForeground(true);
			}
		};
		thread.start();
	}
	private void download(final String id){
		dKApiDownload down = new dKApiDownload(session, "adv_get_song");
		down.addParam("song_id", id);
		down.addParam("fields", "123");
		down.setSyncModus(true);
		down.setGetRawData(true);
		down.setListener(new ApiDownloadListener() {
			
			@Override
			public void onApiDownloadSuccessRaw(ByteArrayOutputStream st) {
				ByteArrayInputStream in = new ByteArrayInputStream(st.toByteArray());
				readSong(in, id);
			}
			
			@Override
			public void onApiDownloadSuccess(JSONObject data) {
			}
			
			@Override
			public void onApiDownloadProgress(int progress) {
				builder.setProgress(100, progress, false);
				startForeground(notifyId, builder.build());
			}
			
			@Override
			public void onApiDownloadError(int code) {
			}
		});
		down.download();
	}
	private void readSong(ByteArrayInputStream in, String id){
		JSONObject attr = null;
		File cover = null;
		File file = null;
		
		ZipInputStream zin = new ZipInputStream(in);
		int size;
        try {
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                String name = ze.getName();
                if(name.endsWith("song_cover")){
                	cover = new File(Paths.getCoverDir(), id + ".jpg");
                	FileOutputStream out = new FileOutputStream(cover, false);
                    BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                    try {
                        while((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            fout.write(buffer, 0, size);
                        }

                        zin.closeEntry();
                    }
                    finally {
                        fout.flush();
                        fout.close();
                    }
                }else if(name.endsWith("song_file")){
                	file = new File(Paths.getAdvSongDir(), id + ".file");
                	FileOutputStream out = new FileOutputStream(file, false);
                    BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                    try {
                        while((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            fout.write(buffer, 0, size);
                        }

                        zin.closeEntry();
                    }
                    finally {
                        fout.flush();
                        fout.close();
                    }
                }else if(name.endsWith("attr")){
                	byte[] bytes= new byte[(int) ze.getSize()];
                    zin.read(bytes, 0, bytes.length);
                    try {
						attr = new JSONObject(new String(bytes, "UTF-8" ));
					} catch (JSONException e) {
						e.printStackTrace();
					}
                	zin.closeEntry();
                }
            }
            if(attr != null && cover != null && file != null){
            	if(cover.exists() && file.exists()){
					try {
						int song_id = -1;
	            		String title = attr.getString("title");
	            		String artist = attr.getString("artist");
	            		String album = attr.getString("album");
	            		String genre = attr.getString("genre");
	            		String coverA;
	            		if(cover.length() < 10){
	            			coverA = "no";
	            		}else{
	            			coverA = id;
	            		}
	            		Log.d("cover", coverA);
	            		int rating = attr.getInt("rating");
	            		int type = attr.getInt("type");
	            		SQLSong song = new SQLSong(song_id, title, artist, album, genre, coverA, rating, 0, file.getAbsolutePath(), 0, type, 0);
	            		SQLiteDataSource dataSource = new SQLiteDataSource(this);
	            		dataSource.open();
	            		dataSource.addSong(song);
	            		dataSource.close();
	            		
	            		AdvSQLiteDataSource advDataSource = new AdvSQLiteDataSource(this);
	            		advDataSource.open();
	            		AdvSong advSong;
	            		advSong = advDataSource.getAdvSongFromServerId(id);
	            		if(advSong == null){
	            			advSong = new AdvSong(id, song.getId(), 0, 0);
	            			advDataSource.addAdvSong(advSong);
	            		}else{
	            			advSong.setLocalId(song.getId());
	            			advDataSource.updateAdvSong(advSong);
	            		}
	            		
	            		advDataSource.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        finally {
            try {
				zin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	public class MyBinder extends Binder {
	    DownloadService getService() {
	      return DownloadService.this;
	    }
	  }

}
