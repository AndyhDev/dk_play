package com.dk.play.adv;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.util.MD5FileCache;

public class AdvSQLiteDataSource {
	private final String TAG = "AdvSQLiteDataSource";
	private SQLiteDatabase database;
	private AdvSQLiteHelper dbHelper;
	@SuppressWarnings("unused")
	private Context ctx;

	public String[] allSongsColumns = { AdvSQLiteHelper.COLUMN_ID,
			AdvSQLiteHelper.COLUMN_SERVER_ID,
			AdvSQLiteHelper.COLUMN_LOCAL_ID,
			AdvSQLiteHelper.COLUMN_STATE,
			AdvSQLiteHelper.COLUMN_LAST_CHECK,};

	public String[] allLocalIdColumns = { AdvSQLiteHelper.COLUMN_ID,
			AdvSQLiteHelper.COLUMN_SERVER_ID,
			AdvSQLiteHelper.COLUMN_LOCAL_ID};

	public String[] allRemovedColumns = { AdvSQLiteHelper.COLUMN_ID,
			AdvSQLiteHelper.COLUMN_LOCAL_ID,
			AdvSQLiteHelper.COLUMN_SERVER_ID,
			AdvSQLiteHelper.COLUMN_TITLE,
			AdvSQLiteHelper.COLUMN_ARTIST};
	private MD5FileCache md5Cache;

	public AdvSQLiteDataSource(Context context){
		ctx = context;
		dbHelper = new AdvSQLiteHelper(context);
		md5Cache = new MD5FileCache();
	}
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	public void close() {
		dbHelper.close();
	}
	public SQLiteDatabase getDatabase(){
		return database;
	}
	public void reCreateDatabase(){
		dbHelper.reCreateDB(database);
	}
	public void addAdvSong(AdvSong song){
		if(getAdvSongFromServerId(song.getServerId(), false) == null){
			song.SQLInsert(database);
		}
	}
	public void updateAdvSong(AdvSong song){
		song.SQLUpdate(database);
	}
	public AdvSong getAdvSong(long songId){
		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_SONGS, allSongsColumns, AdvSQLiteHelper.COLUMN_ID + "=?", new String[]{Long.toString(songId)}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 1){
			return new AdvSong(cursor);
		}
		return null;
	}
	public AdvSong getAdvSongFromServerId(String serverId){
		return getAdvSongFromServerId(serverId, true);
	}
	public AdvSong getAdvSongFromServerId(String serverId, boolean create){
		Log.d(TAG, "1");
		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_SONGS, allSongsColumns, AdvSQLiteHelper.COLUMN_SERVER_ID + "=?", new String[]{serverId}, null, null, null);
		cursor.moveToFirst();
		Log.d(TAG, "count:" + cursor.getCount() );
		if(cursor.getCount() == 1){
			Log.d(TAG, "2");
			return new AdvSong(cursor);
		}else{
			if(create){
				Log.d(TAG, "3");
				return genAdvSong(serverId);
			}
		}
		return null;
	}
	public AdvSong getAdvSongFromSQLSong(SQLSong song){
		AdvSong s = getAdvSongFromSQLSong(song.getId());
		if(s == null){
			return genAdvSong(song);
		}
		return s;
	}
	public AdvSong getAdvSongFromSQLSong(long songId){
		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_SONGS, allSongsColumns, AdvSQLiteHelper.COLUMN_LOCAL_ID + "=?", new String[]{Long.toString(songId)}, null, null, null);
		cursor.moveToFirst();
		Log.d(TAG, "count:" + cursor.getCount() );
		if(cursor.getCount() == 1){
			return new AdvSong(cursor);
		}
		return null;
	}

	private AdvSong genAdvSong(SQLSong song){
		String serverId = md5Cache.getMd5(song.getPath());
		AdvSong advSong = new AdvSong(serverId, song.getId(), AdvSong.STATE_INVALID, System.currentTimeMillis()/1000);
		addAdvSong(advSong);
		return advSong;
	}
	private AdvSong genAdvSong(String serverId){
		String path = md5Cache.getPathFromMd5(serverId);
		Log.d(TAG, "4");
		if(path != null){
			Log.d(TAG, "5");
			Log.d(TAG, path);
			SQLiteDataSource dt = new SQLiteDataSource(ctx);
			dt.open();
			SQLSong song = dt.getSQLSong(path);
			dt.close();
			if(song != null){
				Log.d(TAG, "6");
				AdvSong advSong = new AdvSong(serverId, song.getId(), AdvSong.STATE_INVALID, System.currentTimeMillis()/1000);
				addAdvSong(advSong);
				return advSong;
			}
		}
		return null;
	}
	public void removeAdvSong(AdvSong song) {
		song.SQLDelete(database);
	}
	public List<AdvSong> getAllAdvSongs(){
		List<AdvSong> songs= new ArrayList<AdvSong>();

		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_SONGS, allSongsColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			songs.add(new AdvSong(cursor));
			cursor.moveToNext();
		}
		cursor.close();

		return songs;
	}
	public List<AdvRemoved> getAllAdvRemoved(){
		List<AdvRemoved> rm= new ArrayList<AdvRemoved>();

		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_REMOVED, allRemovedColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			rm.add(new AdvRemoved(cursor));
			cursor.moveToNext();
		}
		cursor.close();

		return rm;
	}
	public boolean isRemoved(Long localId){
		Cursor cursor = database.query(AdvSQLiteHelper.TABLE_REMOVED, allRemovedColumns, AdvSQLiteHelper.COLUMN_LOCAL_ID + "=?", new String[]{Long.toString(localId)}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() >= 1){
			return true;
		}
		return false;
	}
	public void addAdvRemoved(AdvRemoved rm){
		rm.SQLInsert(database);
	}
	public void updateAdvRemoved(AdvRemoved rm){
		rm.SQLUpdate(database);
	}
	public void removeAdvRemoved(AdvRemoved rm){
		rm.SQLDelete(database);
	}
}
