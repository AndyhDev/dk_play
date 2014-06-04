package com.dk.play.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLRemoved {
	private long id;
	private String path;
	
	public SQLRemoved(){
		
	}
	public SQLRemoved(Cursor cursor){
		this.id = cursor.getLong(0);
	    this.path = cursor.getString(1);
	}
	public SQLRemoved(int id, String path){
		this.id = id;
		this.path = path;
	}
	public long SQLInsert(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_SONG, path);
		long insertId = database.insert(SQLiteHelper.TABLE_REMOVED, null, values);
		
		return insertId;
	}
	public void SQLDelete(SQLiteDatabase database){
		database.delete(SQLiteHelper.TABLE_REMOVED, SQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	public void SQLUpdate(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_SONG, path);
		
		database.update(SQLiteHelper.TABLE_REMOVED, values, SQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	public long getId() {
		return id;
	}
	public void setId(long l) {
		this.id = l;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
