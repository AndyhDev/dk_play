package com.dk.play.database;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLPlaylist {
	@SuppressWarnings("unused")
	private static final String TAG = "SQLPlaylist";
	private long id;
	private String name;
	private JSONArray items;
	
	public SQLPlaylist(){
	}
	public SQLPlaylist(String name){
		this.name = name;
		this.items = new JSONArray();
	}
	public SQLPlaylist(Cursor cursor){
		this.id = cursor.getLong(0);
	    this.name = cursor.getString(1);
	    try {
			this.items = new JSONArray(cursor.getString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public SQLPlaylist(int id, String name, JSONArray items){
		this.id = id;
		this.name = name;
		this.items = items;
	}
	
	public long SQLInsert(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, name);
		values.put(SQLiteHelper.COLUMN_ITEMS, items.toString());
		long insertId = database.insert(SQLiteHelper.TABLE_PLAYLIST, null, values);
		this.id = insertId;
		
		return insertId;
		
	}
	public void SQLDelete(SQLiteDatabase database){
		database.delete(SQLiteHelper.TABLE_PLAYLIST, SQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	public void SQLUpdate(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, name);
		values.put(SQLiteHelper.COLUMN_ITEMS, items.toString());
		
		database.update(SQLiteHelper.TABLE_PLAYLIST, values, SQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	
	public SQLSongList getSQLSongList(Context context){
		SQLiteDataSource datasource = new SQLiteDataSource(context);
		datasource.open();
		SQLSongList songList = datasource.getSQLSongList();
		datasource.close();
		
		SQLSongList list = new SQLSongList();
		for (int i = 0; i < items.length(); i++) {
			try {
				long id = items.getLong(i);
				list.add(songList.getById(id));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}		
		return list;
	}
	public Boolean isSQLSongIn(SQLSong song){
		if(items == null){
			return false;
		}
		for (int i = 0; i < items.length(); i++) {
			try {
				long id = items.getLong(i);
				if(song.getId() == id){
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			};
		}
		return false;
	}
	public int getIndexOfSQLSong(SQLSong song){
		if(items == null){
			return 0;
		}
		for (int i = 0; i < items.length(); i++) {
			try {
				long id = items.getLong(i);
				if(song.getId() == id){
					return i;
				};
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	public void removeSQLSong(SQLSong song){
		JSONArray newA = new JSONArray();
		if(items == null){
			items = newA;
		}
		for (int i = 0; i < items.length(); i++) {
			try {
				long id = items.getLong(i);
				if(song.getId() != id){
					newA.put(id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			};
		}
		items = newA;
	}
	public void addSong(SQLSong song){
		items.put(song.getId());
	}
	public void addSong(long id){
		items.put(id);
	}
	public void moveSQLSong(int oldIndex, int newIdex){
		JSONArray newA = new JSONArray();
		if(items == null){
			items = newA;
		}
		try {
			Object item = items.get(oldIndex);
			Object obj;
			int count = 0;
			for (int i = 0; i < items.length(); i++) {
				try {
					if(count == newIdex){
						//Log.d("JSONArrayMove", "i='" + i + "', item='" + item + "'");
						newA.put(item);
						count++;
					}
					if(i != oldIndex){
						obj = items.get(i);
						//Log.d("JSONArrayMove", "i='" + i + "', item='" + obj + "'");
						newA.put(obj);
						count++;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				};
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		items = newA;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JSONArray getItems() {
		return items;
	}

	public void setItems(JSONArray items) {
		this.items = items;
	}
}
