package com.dk.play.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper{
	public static final String TABLE_SONGS = "songs";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_ARTIST = "artist";
	public static final String COLUMN_ALBUM = "album";
	public static final String COLUMN_GENRE = "genre";
	public static final String COLUMN_COVER = "cover";
	public static final String COLUMN_RATING = "rating";
	public static final String COLUMN_PLAY_COUNT = "play_count";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_CLICK = "click";
	
	public static final String TABLE_PLAYLIST = "playlists";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_ITEMS = "itmes";

	public static final String TABLE_REMOVED = "removed";
	public static final String COLUMN_SONG = "song";

	private static final String DATABASE_NAME = "songs.db";
	private static final int DATABASE_VERSION = 1;


	private static final String DATABASE_SONGS_CREATE = "create table " + TABLE_SONGS + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_TITLE + " VARCHAR(250) not null,"
			+ COLUMN_ARTIST + " VARCHAR(250) not null,"
			+ COLUMN_ALBUM + " VARCHAR(250) not null,"
			+ COLUMN_GENRE + " VARCHAR(20) not null,"
			+ COLUMN_COVER + " TEXT not null,"
			+ COLUMN_RATING + " INTEGER not null,"
			+ COLUMN_PLAY_COUNT + " INTEGER not null,"
			+ COLUMN_PATH + " TEXT not null,"
			+ COLUMN_TIME + " INTEGER not null,"
			+ COLUMN_TYPE + " INTEGER not null,"
			+ COLUMN_CLICK + " INTEGER not null"
			+");";

	private static final String DATABASE_PLAYLITS_CREATE = "create table " + TABLE_PLAYLIST + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_NAME + " VARCHAR(250) not null,"
			+ COLUMN_ITEMS + " TEXT not null"
			+");";

	private static final String DATABASE_REMOVED_CREATE = "create table " + TABLE_REMOVED + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_SONG + " TEXT not null"
			+");";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void reCreateDB(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMOVED);
		onCreate(db);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_SONGS_CREATE);
		db.execSQL(DATABASE_PLAYLITS_CREATE);
		db.execSQL(DATABASE_REMOVED_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMOVED);
		onCreate(db);
	}

}
