package com.dk.play.util;

import android.util.Log;

import com.dk.play.App;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.adv.AdvSong;
import com.dk.play.database.SQLSong;

public class CloudItem {
	private String id;
	private String title;
	private String artist;
	private int rating;
	private String genre;
	private int onDevice;
	private boolean expanded = false;
	
	public static final int NOT_ON_DEVICE = 0;
	public static final int ON_DEVICE = 1;
	public static final int ON_CACHE = 2;
	
	public CloudItem(String id, String title, String artist, int rating, String genre, int onDevice){
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.rating = rating;
		this.genre = genre;
		this.onDevice = onDevice;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
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
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public int getOnDevice() {
		return onDevice;
	}
	public void setOnDevice(int onDevice) {
		this.onDevice = onDevice;
	}
	public SQLSong getSQLSong(){
		if(onDevice == ON_DEVICE){
			AdvSQLiteDataSource dataSource = new AdvSQLiteDataSource(App.getContextStatic());
			dataSource.open();
			Log.d("ID", "Server_id2:" + id);
			AdvSong advSong = dataSource.getAdvSongFromServerId(id);
			Log.d("ID", "local_id2:" + advSong.getLocalId());
			dataSource.close();
			return advSong.getSQLSong();
		}
		return null;
	}
	public void toggleExpand() {
		if(expanded){
			expanded = false;
		}else{
			expanded = true;
		}
	}
	public boolean getExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
}
