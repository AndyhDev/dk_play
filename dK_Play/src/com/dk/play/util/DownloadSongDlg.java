package com.dk.play.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.database.SQLSong;
import com.dk.util.IO;
import com.dk.util.network.ApiDownloadListener;
import com.dk.util.network.dKApiDownload;
import com.dk.util.network.dKSession;

public class DownloadSongDlg implements OnClickListener, OnCancelListener {
	private Context context;
	private Activity activity;
	private CloudItem item;
	private String title;
	private String message;
	private dKApiDownload down;
	private AdvControl adv;
	private ProgressDialog dialog;
	private DownloadSongDlgListener listener;
	private int BUFFER_SIZE = 1024;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private boolean stopped = false;
	
	public DownloadSongDlg(Activity activity, CloudItem item){
		this.activity = activity;
		this.context = activity;
		this.item = item;
		adv = new AdvControl(context);
		title = context.getString(R.string.download_cloud_song);
		message = item.getTitle();
		setUp();
	}

	private void setUp() {
		dialog = new ProgressDialog(context);;
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setCancelable(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setIndeterminate(true);
		dialog.setOnCancelListener(this);
	}
	private void setProgress(int progress){
		dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgress(progress);
	}
	public void setListener(DownloadSongDlgListener listener){
		this.listener = listener;
	}
	public void show(){
		dialog.show();
		Thread thread = new Thread(){
			@Override
			public void run() {
				dKSession session = adv.getSession();
				if(session != null){
					if(stopped){
						dialog.dismiss();
						return;
					}
					down = new dKApiDownload(activity, session, "adv_get_song");
					down.addParam("song_id", item.getId());
					down.addParam("fields", "123");
					down.setGetRawData(true);
					down.setListener(new ApiDownloadListener() {
						
						@Override
						public void onApiDownloadSuccessRaw(ByteArrayOutputStream st) {
							ByteArrayInputStream in = new ByteArrayInputStream(st.toByteArray());
							readSong(in);
							dialog.dismiss();
						}
						
						@Override
						public void onApiDownloadSuccess(JSONObject data) {
						}
						
						@Override
						public void onApiDownloadProgress(int progress) {
							Log.d("PROGRESS", "progress:" + progress);
							setProgress(progress);
						}
						
						@Override
						public void onApiDownloadError(int code) {
							dialog.dismiss();
						}
					});
					down.download();
				}else{
					dialog.dismiss();
				}
				
			}
		};
		thread.start();
	}
	private void readSong(ByteArrayInputStream in){
		JSONObject attr = null;
		File cover = null;
		File file = null;
		File attr_file = null;
		
		ZipInputStream zin = new ZipInputStream(in);
		int size;
        try {
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                String name = ze.getName();
                if(name.endsWith("song_cover")){
                	cover = new File(context.getCacheDir(), item.getId() + ".cover");
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
                	file = new File(context.getCacheDir(), item.getId() + ".file");
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
                	attr_file = new File(context.getCacheDir(), item.getId() + ".attr");
                	byte[] bytes= new byte[(int) ze.getSize()];
                    zin.read(bytes, 0, bytes.length);
                    try {
						attr = new JSONObject(new String(bytes, "UTF-8" ));
						IO.write(attr.toString(), attr_file);
					} catch (JSONException e) {
						e.printStackTrace();
					}
                	zin.closeEntry();
                }
            }
            if(attr != null && cover != null && file != null){
            	if(cover.exists() && file.exists()){
					try {
						int id = -1;
	            		String title = attr.getString("title");
	            		String artist = attr.getString("artist");
	            		String album = attr.getString("album");
	            		String genre = attr.getString("genre");
	            		String coverA;
	            		if(cover.length() < 10){
	            			coverA = "no";
	            		}else{
	            			coverA = cover.getAbsolutePath();
	            		}
	            		Log.d("cover", coverA);
	            		int rating = attr.getInt("rating");
	            		int type = attr.getInt("type");
	            		SQLSong song = new SQLSong(id, title, artist, album, genre, coverA, rating, 0, file.getAbsolutePath(), 0, type, 0);
	            		song.setDontSQL(true);
	            		if(!coverA.equals("no")){
	            			Log.d("cover", "usepath");
	            			song.setCoverUsePath(true);
	            		}
	            		if(listener != null){
	            			if(!stopped){
	            				listener.onSong(song);
	            			}
	            		}
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
	public void onClick(DialogInterface dialog, int which) {
		//dialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		stopped = true;
		if(down != null){
			down.stop();
		}
	}
}
