package com.dk.play.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;

import com.dk.play.App;
import com.dk.play.R;
import com.dk.play.util.Image;
import com.dk.play.util.Paths;

public class SQLSong {
	@SuppressWarnings("unused")
	private static final String TAG = "SQLSong";
	private long id;
	private String title;
	private String artist;
	private String album;
	private String genre;
	private String cover;
	private int rating;
	private int play_count;
	private String path;
	private long time;
	private int type;
	private int click;
	
	private boolean dontSQL = false;
	private boolean coverUsePath = false;
	
	public static final int TYPE_MUSIC = 0;
	public static final int TYPE_VIDEO = 1;

	public SQLSong(){

	}
	public SQLSong(SQLSong song){
		this.id = song.getId();
		this.title = song.getTitle();
		this.artist = song.getArtist();
		this.album = song.getAlbum();
		this.genre = song.getGenre();
		this.cover = song.getCover();
		this.rating = song.getRating();
		this.play_count = song.getPlay_count();
		this.path = song.getPath();
		this.time = song.getTime();
		this.type = song.getType();
		this.click = song.getClick();
	}
	public SQLSong(Cursor cursor){
		this.id = cursor.getLong(0);
		this.title = cursor.getString(1);
		this.artist = cursor.getString(2);
		this.album = cursor.getString(3);
		this.genre = cursor.getString(4);
		this.cover = cursor.getString(5);
		this.rating = cursor.getInt(6);
		this.play_count = cursor.getInt(7);
		this.path = cursor.getString(8);
		this.time = cursor.getLong(9);
		this.type = cursor.getInt(10);
		this.click = cursor.getInt(11);
	}
	public SQLSong(int id, String title, String artist, String album, String genre, String cover, int rating, int play_count, String path, long time, int type, int click){
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.genre = genre;
		this.cover = cover;
		this.rating = rating;
		this.play_count = play_count;
		this.path = path;
		this.time = time;
		this.type = type;
		this.click = click;
	}
	public long SQLInsert(SQLiteDatabase database){
		if(dontSQL){
			return 0;
		}
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_TITLE, title);
		values.put(SQLiteHelper.COLUMN_ARTIST, artist);
		values.put(SQLiteHelper.COLUMN_ALBUM, album);
		values.put(SQLiteHelper.COLUMN_GENRE, genre);
		values.put(SQLiteHelper.COLUMN_COVER, cover);
		values.put(SQLiteHelper.COLUMN_RATING, rating);
		values.put(SQLiteHelper.COLUMN_PLAY_COUNT, play_count);
		values.put(SQLiteHelper.COLUMN_PATH, path);
		values.put(SQLiteHelper.COLUMN_TIME, time);
		values.put(SQLiteHelper.COLUMN_TYPE, type);
		values.put(SQLiteHelper.COLUMN_CLICK, click);
		long insertId = database.insert(SQLiteHelper.TABLE_SONGS, null, values);
		this.id = insertId;
		return insertId;
	}
	public void SQLDelete(SQLiteDatabase database){
		if(dontSQL){
			return;
		}
		database.delete(SQLiteHelper.TABLE_SONGS, SQLiteHelper.COLUMN_ID + " = " + id, null);
	}
	public void SQLUpdate(SQLiteDatabase database){
		if(dontSQL){
			return;
		}
		setTime(System.currentTimeMillis()/1000);
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_TITLE, title);
		values.put(SQLiteHelper.COLUMN_ARTIST, artist);
		values.put(SQLiteHelper.COLUMN_ALBUM, album);
		values.put(SQLiteHelper.COLUMN_GENRE, genre);
		values.put(SQLiteHelper.COLUMN_COVER, cover);
		values.put(SQLiteHelper.COLUMN_RATING, rating);
		values.put(SQLiteHelper.COLUMN_PLAY_COUNT, play_count);
		values.put(SQLiteHelper.COLUMN_PATH, path);
		values.put(SQLiteHelper.COLUMN_TIME, time);
		values.put(SQLiteHelper.COLUMN_TYPE, type);
		values.put(SQLiteHelper.COLUMN_CLICK, click);
		database.update(SQLiteHelper.TABLE_SONGS, values, SQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	public void SQLUpdateWithoutTime(SQLiteDatabase database){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_TITLE, title);
		values.put(SQLiteHelper.COLUMN_ARTIST, artist);
		values.put(SQLiteHelper.COLUMN_ALBUM, album);
		values.put(SQLiteHelper.COLUMN_GENRE, genre);
		values.put(SQLiteHelper.COLUMN_COVER, cover);
		values.put(SQLiteHelper.COLUMN_RATING, rating);
		values.put(SQLiteHelper.COLUMN_PLAY_COUNT, play_count);
		values.put(SQLiteHelper.COLUMN_PATH, path);
		values.put(SQLiteHelper.COLUMN_TIME, time);
		values.put(SQLiteHelper.COLUMN_TYPE, type);
		values.put(SQLiteHelper.COLUMN_CLICK, click);
		database.update(SQLiteHelper.TABLE_SONGS, values, SQLiteHelper.COLUMN_ID + "=" + id, null);
	}
	public long getId() {
		return id;
	}
	public void setId(long l) {
		this.id = l;
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
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getCover() {
		return cover;
	}
	public boolean hasCover(){
		if(cover == "no"){
			return false;
		}
		return true;
	}
	public Uri getCoverUri(){
		if(cover == "no"){
			return Uri.parse("android.resource://your.package.name/" + R.drawable.default_cover);
		}
		if(coverUsePath){
			Uri uri = Uri.parse("file://" + cover);
			return uri;

		}else{
			Uri uri = Uri.parse("file://" + Paths.getCoverPath(cover));
			return uri;
		}
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public void setCoverBitmap(Bitmap bmp){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] data = stream.toByteArray();
		File file;
		if(coverUsePath){
			file = new File(cover);
		}else{
			file = new File(Paths.getCoverPath(cover));
		}

		try {
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			fOut.write(data);
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Bitmap getCoverBitmap(int size){
		if(cover == "no"){
			return Image.decodeSampledBitmapFromResource(App.getResourcesStatic(), R.drawable.default_cover, size, size);
		}
		String path;
		if(coverUsePath){
			path = cover;
		}else{
			path = Paths.getCoverPath(cover);
		}
		File f = new File(path);
		if(!f.exists()){
			return Image.decodeSampledBitmapFromResource(App.getResourcesStatic(), R.drawable.default_cover, size, size);
		}
		return Image.decodeSampledBitmapFromPath(path, size, size);
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public int getPlay_count() {
		return play_count;
	}
	public void setPlay_count(int play_count) {
		this.play_count = play_count;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getClick() {
		return click;
	}
	public void setClick(int click) {
		this.click = click;
	}
	public void playcountUp() {
		play_count++;
	}
	public void click(){
		click++;
	}
	public boolean getDontSQL() {
		return dontSQL;
	}
	public void setDontSQL(boolean dontSQL) {
		this.dontSQL = dontSQL;
	}
	public boolean getCoverUsePath() {
		return coverUsePath;
	}
	public void setCoverUsePath(boolean coverUsePath) {
		this.coverUsePath = coverUsePath;
	}
}
