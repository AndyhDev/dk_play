package com.dk.play.adv;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dk.play.App;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.util.AdvUploadSong2;
import com.dk.play.util.Paths;

public class AdvSong {
	public static final int STATE_INVALID = 0;
	public static final int STATE_OK = 1;
	private long id;
	private String serverId;
	private long localId;
	private int state;
	private long lastCheck;
	
	public AdvSong(){
		
	}
	
	public AdvSong(AdvSong advSong){
		this.id = advSong.getId();
		this.serverId = advSong.getServerId();
		this.localId = advSong.getLocalId();
		this.state = advSong.getState();
		this.lastCheck= advSong.getLastCheck();
	}
	
	public AdvSong(Cursor cursor){
		this.id = cursor.getLong(0);
		this.serverId = cursor.getString(1);
		this.localId = cursor.getLong(2);
		this.state = cursor.getInt(3);
		this.lastCheck = cursor.getLong(4);
	}
	
	public AdvSong(String serverId, long localId, int state, long lastUpdate){
		this.serverId = serverId;
		this.localId = localId;
		this.state = state;
		this.lastCheck = lastUpdate;
	}
	public long SQLInsert(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(AdvSQLiteHelper.COLUMN_SERVER_ID, serverId);
		values.put(AdvSQLiteHelper.COLUMN_LOCAL_ID, localId);
		values.put(AdvSQLiteHelper.COLUMN_STATE, state);
		values.put(AdvSQLiteHelper.COLUMN_LAST_CHECK, lastCheck);
		long insertId = database.insert(AdvSQLiteHelper.TABLE_SONGS, null, values);
		this.id = insertId;
		
		return insertId;
	}
	public void SQLDelete(SQLiteDatabase database){
		database.delete(AdvSQLiteHelper.TABLE_SONGS, AdvSQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	public void SQLUpdate(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(AdvSQLiteHelper.COLUMN_SERVER_ID, serverId);
		values.put(AdvSQLiteHelper.COLUMN_LOCAL_ID, localId);
		values.put(AdvSQLiteHelper.COLUMN_STATE, state);
		values.put(AdvSQLiteHelper.COLUMN_LAST_CHECK, lastCheck);
		database.update(AdvSQLiteHelper.TABLE_SONGS, values, AdvSQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getLocalId() {
		return localId;
	}

	public void setLocalId(long localId) {
		this.localId = localId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(long lastCheck) {
		this.lastCheck = lastCheck;
	}
	
	public SQLSong getSQLSong(){
		SQLiteDataSource dataSource =  new SQLiteDataSource(App.getContextStatic());
		dataSource.open();
		SQLSong song = dataSource.getSQLSong(localId);
		dataSource.close();
		return song;
	}

	public boolean storeNewAttr(String attr) {
		SQLSong song = getSQLSong();
		boolean insert = false;
		if(song == null){
			song = new SQLSong();
			insert = true;
		}
		try {
			JSONObject data = new JSONObject(attr);
			song.setTitle(data.getString(AdvUploadSong2.songTitle));
			song.setArtist(data.getString(AdvUploadSong2.songArtist));
			song.setAlbum(data.getString(AdvUploadSong2.songAlbum));
			song.setGenre(data.getString(AdvUploadSong2.songGenre));
			song.setRating(data.getInt(AdvUploadSong2.songRating));
			song.setPlay_count(data.getInt(AdvUploadSong2.songPlayCount));
			song.setTime(data.getLong(AdvUploadSong2.songTime));
			song.setType(data.getInt(AdvUploadSong2.songType));
			song.setClick(data.getInt(AdvUploadSong2.songClick));
			song.setCover(data.getString(AdvUploadSong2.songCoverName));
			
			String name = data.getString(AdvUploadSong2.songFileName);
			File f = Paths.getAdvSongDir();
			if(song.getPath() == null){
				String path = new File(f, name).getAbsolutePath();
				song.setPath(path);
			}
			SQLiteDataSource dataSource =  new SQLiteDataSource(App.getContextStatic());
			dataSource.open();
			if(insert){
				dataSource.addSong(song);
				localId = song.getId();
			}else{
				dataSource.updateSongWithoutTime(song);
			}
			dataSource.close();
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean valid(){
		if(getSQLSong() != null){
			return true;
		}
		return false;
	}
}
