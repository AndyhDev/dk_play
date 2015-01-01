package com.dk.play.adv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AdvSQLiteHelper extends SQLiteOpenHelper{
	public static final String TABLE_SONGS = "adv_songs";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SERVER_ID = "server_id";
	public static final String COLUMN_LOCAL_ID = "local_id";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_LAST_CHECK = "last_check";
	
	public static final String TABLE_LOCAL_ID = "local_id";

	public static final String TABLE_REMOVED = "removed";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_ARTIST = "artist";
	
	private static final String DATABASE_NAME = "adv.db";
	private static final int DATABASE_VERSION = 9;


	private static final String DATABASE_SONGS_CREATE = "create table " + TABLE_SONGS + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_SERVER_ID + " VARCHAR(250) not null,"
			+ COLUMN_LOCAL_ID + " INTEGER not null,"
			+ COLUMN_STATE + " INTEGER not null,"
			+ COLUMN_LAST_CHECK + " INTEGER not null"
			+");";

	private static final String DATABASE_LOCAL_ID_CREATE = "create table " + TABLE_LOCAL_ID + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_SERVER_ID + " VARCHAR(250) not null,"
			+ COLUMN_LOCAL_ID + " INTEGER not null"
			+");";

	private static final String DATABASE_REMOVED_CREATE = "create table " + TABLE_REMOVED + "("
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_LOCAL_ID + " INTEGER not null,"
			+ COLUMN_SERVER_ID + " VARCHAR(250) not null,"
			+ COLUMN_TITLE + " VARCHAR(250) not null,"
			+ COLUMN_ARTIST + " VARCHAR(250) not null"
			+");";

	public AdvSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void reCreateDB(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCAL_ID);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMOVED);
		onCreate(db);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_SONGS_CREATE);
		db.execSQL(DATABASE_LOCAL_ID_CREATE);
		db.execSQL(DATABASE_REMOVED_CREATE);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(AdvSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCAL_ID);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMOVED);
		onCreate(db);
	}

}
