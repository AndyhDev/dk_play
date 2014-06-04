package com.dk.play.util;

import android.net.Uri;

public class Song {
	private long id;
	private String title;
	private String artist;
	private int rating;
	private int played;
	private Uri uri;
	
	public Song(long songID, String songTitle, String songArtist, int songRating, int songPlayed, Uri songUri){
		id = songID;
		title = songTitle;
		artist = songArtist;
		rating = songRating;
		played = songPlayed;
		uri = songUri;
	}
	public long getID(){return id;}
	public String getTitle(){return title;}
	public String getArtist(){return artist;}
	public int getRating(){return rating;}
	public int getPlayed(){return played;}
	public Uri getUri(){return uri;}
}
