package com.dk.play.util;

import com.dk.play.database.SQLSong;

public abstract class SearchListener {
	public abstract void onStart();
	public abstract void onFound(SQLSong song);
	public abstract void onEnd();
}
