package com.dk.play.util;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.dk.play.database.SQLSong;
import com.dk.play.service.AdvService;
import com.dk.util.Hash;
import com.dk.util.network.Upload;
import com.dk.util.network.UploadListener;
import com.dk.util.network.dKSession;

public class AdvUploadSong2 implements UploadListener {
	private static final String TAG = "AdvUploadSong";
	
	private SQLSong song;
	private dKSession session;
	private String fileName;
	
	public static final String songTitle = "title";
	public static final String songId = "song_id";
	public static final String songCover = "song_cover";
	public static final String songCoverName = "cover_name";
	public static final String songFile = "song_file";
	public static final String songFileName = "file_name";
	public static final String songAttr = "attr"; 
	public static final String songArtist = "artist";
	public static final String songAlbum = "album";
	public static final String songGenre = "genre";
	public static final String songRating = "rating";
	public static final String songPlayCount = "play_count";
	public static final String songTime = "time";
	public static final String songType = "type";
	public static final String songClick = "click";
	
	private Upload passUpload;
	private boolean passUploadAdded = false;
	
	private AdvUploadSongListener listener;
	
	public static final int NO_ERROR = 0;
	public static final int ERROR_FILE_TOO_BIG = 1;
	public static final int ERROR_NOT_SEND = 2;
	
	private MD5FileCache md5Cache;
	private int lastProgress = 0;
	private String attrHash = null;
	private String attrCache = null;
	
	public AdvUploadSong2(SQLSong song, dKSession session){
		this.song = song;
		this.session = session;
		
		md5Cache = new MD5FileCache();
		fileName = new File(song.getPath()).getName();
		
	}
	
	public int run(boolean upFile, boolean upCover, boolean upAttr){
		startPassUpload(md5Cache.getMd5(song.getPath()), String.valueOf(song.getTime()));
		if(upFile){
			Log.d(TAG, "upload addFile");
			addFileTooPass(songFile, song.getPath());
		}else{
			Log.d(TAG, "upload not addFile");
		}
		if(upCover){
			Log.d(TAG, "upload addCover");
			if(song.getCover().equals("no")){
				addParamTooPass(songCover, "no");
			}else{
				addFileTooPass(songCover, song.getCoverUri().getPath());
			}
		}else{
			Log.d(TAG, "upload not addCover");
		}
		if(upAttr){
			Log.d(TAG, "upload addAttr");
			addParamTooPass(songAttr, getAttr());
		}else{
			Log.d(TAG, "upload not addAttr");
		}
		return sendPassUpload();
	}
	private String getAttr(){
		if(attrCache == null){
			JSONObject obj = new JSONObject();
			try {
				obj.put(songTitle, song.getTitle());
				obj.put(songArtist, song.getArtist());
				obj.put(songAlbum, song.getAlbum());
				obj.put(songGenre, song.getGenre());
				obj.put(songRating, song.getRating());
				obj.put(songPlayCount, song.getPlay_count());
				obj.put(songTime, song.getTime());
				obj.put(songType, song.getType());
				obj.put(songClick, song.getClick());
				obj.put(songCoverName, song.getCover());
				obj.put(songFileName, fileName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			attrCache = obj.toString();
		}
		return attrCache;
	}
	public String getAttrMD5(){
		if(attrHash == null){
			attrHash = Hash.getMd5(getAttr());
		}
		return attrHash;
	}
	public String sizeFormat(long size){ 
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		long tb = gb * 1024;

		if(size >= 0 && size < kb){
			return String.valueOf(size) + "B";

		}else if(size >= kb && size < mb) {
			return String.valueOf(size / kb) + "KB";

		}else if(size >= mb && size < gb) {
			return String.valueOf(size / mb) + "MB";

		}else if(size >= gb && size < tb) {
			return String.valueOf(size / gb) + "GB";

		} else if(size >= tb) {
			return String.valueOf(size / tb) + "TB";
		} else {
			return size + "B";
		}
	}
	
	private void startPassUpload(String id, String time){
		passUploadAdded = false;
		passUpload = new Upload("https://dk-force.de/api/adv_upload_song.php" + session.getUrlGET());
		passUpload.setSyncModus(true);
		passUpload.addParam(songId, id);
		passUpload.addParam(songTime, time);
	}
	private void addFileTooPass(String name, String filePath){
		fileName = new File(song.getPath()).getName();
		passUploadAdded = true;
		passUpload.addFile(name, filePath);
	}
	private void addParamTooPass(String name, String value){
		passUploadAdded = true;
		passUpload.addParam(name, value);
	}
	private int sendPassUpload(){
		if(passUploadAdded){
			Log.d(TAG, "upload 21");
			if(passUpload.getSize() > AdvService.maxUploadSize){
				Log.d(TAG, "upload 22");
				return ERROR_FILE_TOO_BIG;
			}
			Log.d(TAG, "upload 23");
			passUpload.setListener(this);
			String response = passUpload.start();
			if(response != null){
				Log.d(TAG, "upload 24");
				if(response.startsWith("stored")){
					return NO_ERROR;
				}
			}
			Log.d(TAG, "upload 25");
		}
		return ERROR_NOT_SEND;
	}

	public void setListener(AdvUploadSongListener listener) {
		this.listener = listener;
	}

	@Override
	public void onUploadProgress(int progress) {
		if(progress != lastProgress){
			lastProgress = progress;
			if(listener != null){
				listener.onProgress(progress);
			}
		}
	}

	@Override
	public void onUploadEnd(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUploadError() {
		// TODO Auto-generated method stub
		
	}

	public SQLSong getSQLSong() {
		return song;
	}

	public void cancel() {
		if(passUpload != null){
			passUpload.cancel();
		}
	}
	
}
