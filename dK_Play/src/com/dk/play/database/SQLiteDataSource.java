package com.dk.play.database;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.adv.AdvSong;
import com.dk.play.service.PlayService;
import com.dk.play.util.Paths;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteDataSource {
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private Context ctx;
	
	public String[] allSongsColumns = { SQLiteHelper.COLUMN_ID,
		      SQLiteHelper.COLUMN_TITLE,
		      SQLiteHelper.COLUMN_ARTIST,
		      SQLiteHelper.COLUMN_ALBUM,
		      SQLiteHelper.COLUMN_GENRE,
		      SQLiteHelper.COLUMN_COVER,
		      SQLiteHelper.COLUMN_RATING,
		      SQLiteHelper.COLUMN_PLAY_COUNT,
		      SQLiteHelper.COLUMN_PATH,
		      SQLiteHelper.COLUMN_TIME,
		      SQLiteHelper.COLUMN_TYPE,
		      SQLiteHelper.COLUMN_CLICK};
	
	public String[] allPlaylistColumns = { SQLiteHelper.COLUMN_ID,
		      SQLiteHelper.COLUMN_NAME,
		      SQLiteHelper.COLUMN_ITEMS};
	
	public String[] PlaylistColumnName = { SQLiteHelper.COLUMN_ID,
		      SQLiteHelper.COLUMN_NAME};
	
	public String[] allRemovedColumns = { SQLiteHelper.COLUMN_ID,
		      SQLiteHelper.COLUMN_SONG};
	
	private List<String> playlistReservedNames = new ArrayList<String>();
	
	public SQLiteDataSource(Context context){
		ctx = context;
		dbHelper = new SQLiteHelper(context);
		playlistReservedNames.add("main");
		playlistReservedNames.add("lastest");
		playlistReservedNames.add("click");
		playlistReservedNames.add("most");
		playlistReservedNames.add("r5");
		playlistReservedNames.add("r4");
		playlistReservedNames.add("r3");
		playlistReservedNames.add("r2");
		playlistReservedNames.add("r1");
		playlistReservedNames.add("r0");
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
	public void addSong(SQLSong song){
		//database.execSQL(song.createSQLInsert());
		song.SQLInsert(database);
	}
	public void updateSong(SQLSong song){
		song.SQLUpdate(database);
	}
	public void updateSongWithoutTime(SQLSong song){
		song.SQLUpdateWithoutTime(database);
	}
	public SQLSongList getSQLSongList(){
		SQLSongList list = new SQLSongList();

		Cursor cursor = database.query(SQLiteHelper.TABLE_SONGS, allSongsColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLSong song = new SQLSong(cursor);
			list.add(song);
			cursor.moveToNext();
		}
		cursor.close();

		return list;
	}
	public SQLSong getSQLSong(long songId){

		Cursor cursor = database.query(SQLiteHelper.TABLE_SONGS, allSongsColumns, SQLiteHelper.COLUMN_ID + "=?", new String[]{Long.toString(songId)}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 1){
			SQLSong song = new SQLSong(cursor);
			cursor.close();
			return song;
		}
		cursor.close();
		return null;
	}
	public SQLSong getSQLSong(String path){

		Cursor cursor = database.query(SQLiteHelper.TABLE_SONGS, allSongsColumns, SQLiteHelper.COLUMN_PATH + "=?", new String[]{path}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 1){
			SQLSong song = new SQLSong(cursor);
			cursor.close();
			return song;
		}
		cursor.close();
		return null;
	}
	public void addPlaylist(SQLPlaylist playlist){
		playlist.SQLInsert(database);
	}
	public void updatePlaylist(SQLPlaylist playlist){
		playlist.SQLUpdate(database);
		Intent playlistModify = new Intent(PlayService.PLAYLIST_MODIFY);
		playlistModify.putExtra(PlayService.PLAYLIST_NAME, playlist.getName());
		playlistModify.putExtra(PlayService.PLAYLIST_ID, playlist.getId());
		ctx.sendBroadcast(playlistModify);
	}
	public void removePlaylist(SQLPlaylist playlist){
		playlist.SQLDelete(database);
	}
	public List<SQLPlaylist> getAllSQLPlaylists(){
		List<SQLPlaylist> playlists = new ArrayList<SQLPlaylist>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_PLAYLIST, allPlaylistColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLPlaylist playlist = new SQLPlaylist(cursor);
			playlists.add(playlist);
			cursor.moveToNext();
		}
		cursor.close();

		return playlists;
	}
	public Boolean isPlaylist(String name){
		if(playlistReservedNames.indexOf(name) != -1){
			return true;
		}
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLAYLIST, PlaylistColumnName, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String n = cursor.getString(1);
			if(name.equals(n)){
				cursor.close();
				return true;
			}
			cursor.moveToNext();
		}
		cursor.close();
		
		return false;
	}
	public Map<String, Long> getPlaylistsNames(){
		Map<String, Long> names = new HashMap<String, Long>();
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLAYLIST, PlaylistColumnName, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			long id = cursor.getLong(0);
			String n = cursor.getString(1);
			names.put(n, id);
			cursor.moveToNext();
		}
		cursor.close();
		return names;
	}
	public SQLPlaylist getSQLPlaylist(long playlistId) {
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLAYLIST, allPlaylistColumns, SQLiteHelper.COLUMN_ID + "=?", new String[]{Long.toString(playlistId)}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 1){
			return new SQLPlaylist(cursor);
		}
		return null;
	}
	public SQLPlaylist getSQLPlaylist(String playlistName) {
		Cursor cursor = database.query(SQLiteHelper.TABLE_PLAYLIST, allPlaylistColumns, SQLiteHelper.COLUMN_NAME + "=?", new String[]{playlistName}, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 1){
			return new SQLPlaylist(cursor);
		}
		return null;
	}
	public void addRemoved(SQLRemoved item){
		item.SQLInsert(database);
	}
	public void updateRemoved(SQLRemoved item){
		item.SQLUpdate(database);
	}
	public void removeRemoved(SQLRemoved item){
		item.SQLDelete(database);
	}
	public SQLRemovedList getSQLRemovedList(){
		SQLRemovedList list = new SQLRemovedList();

		Cursor cursor = database.query(SQLiteHelper.TABLE_REMOVED, allRemovedColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLRemoved item = new SQLRemoved(cursor);
			list.add(item);
			cursor.moveToNext();
		}
		cursor.close();

		return list;
	}

	public void removeSQLSong(SQLSong song) {
		List<SQLPlaylist> playlists = getAllSQLPlaylists();
		SQLPlaylist playlist;
		for(int i = 0; i < playlists.size(); i++){
			playlist = playlists.get(i);
			if(playlist.isSQLSongIn(song)){
				playlist.removeSQLSong(song);
				playlist.SQLUpdate(database);
			}
		}
		AdvSQLiteDataSource adv = new AdvSQLiteDataSource(ctx);
		adv.open();
		AdvSong advSong = adv.getAdvSongFromSQLSong(song);
		if(advSong != null){
			adv.removeAdvSong(advSong);
		}
		adv.close();
		
		String path = song.getPath();
		if(path.startsWith(Paths.getAdvSongDir().getAbsolutePath())){
			File file = new File(path);
			File cover = new File(song.getCoverUri().getPath());
			file.delete();
			cover.delete();
		}
		
		SQLRemoved rem = new SQLRemoved(0, song.getPath());
		rem.SQLInsert(database);
		song.SQLDelete(database);
	}
}
