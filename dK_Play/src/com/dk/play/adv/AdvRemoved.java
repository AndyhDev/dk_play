package com.dk.play.adv;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AdvRemoved {
	private Long localId;
	private String serverId;
	private Long id;
	private String title;
	private String artist;
	
	public AdvRemoved(){
		
	}
	
	public AdvRemoved(Long id, Long localId, String serverId, String title, String artist){
		this.id = id;
		this.localId = localId;
		this.serverId = serverId;
		this.title = title;
		this.artist = artist;
	}
	
	public AdvRemoved(AdvRemoved removed){
		id = removed.getId();
		localId = removed.getLocalId();
		serverId = removed.getServerId();
		title = removed.getTitle();
		artist = removed.getArtist();
	}
	
	public AdvRemoved(Cursor cursor){
		id = cursor.getLong(0);
		localId = cursor.getLong(1);
		serverId = cursor.getString(2);
		title = cursor.getString(3);
		artist = cursor.getString(4);
	}
	public long SQLInsert(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(AdvSQLiteHelper.COLUMN_LOCAL_ID, localId);
		values.put(AdvSQLiteHelper.COLUMN_SERVER_ID, serverId);
		values.put(AdvSQLiteHelper.COLUMN_TITLE, title);
		values.put(AdvSQLiteHelper.COLUMN_ARTIST, artist);
		long insertId = database.insert(AdvSQLiteHelper.TABLE_REMOVED, null, values);
		this.id = insertId;
		
		return insertId;
	}
	public void SQLDelete(SQLiteDatabase database){
		database.delete(AdvSQLiteHelper.TABLE_REMOVED, AdvSQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	public void SQLUpdate(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(AdvSQLiteHelper.COLUMN_LOCAL_ID, localId);
		values.put(AdvSQLiteHelper.COLUMN_SERVER_ID, serverId);
		values.put(AdvSQLiteHelper.COLUMN_TITLE, title);
		values.put(AdvSQLiteHelper.COLUMN_ARTIST, artist);
		database.update(AdvSQLiteHelper.TABLE_REMOVED, values, AdvSQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	
	public Long getLocalId() {
		return localId;
	}
	public void setLocalId(Long localId) {
		this.localId = localId;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}
}
