package com.dk.play.util;

import com.dk.play.database.SQLSong;

import android.widget.ImageView;
import android.widget.TextView;

public class SongViewHolder{
    public TextView title;
    public TextView artist;
    public ImageView cover;
    public ImageView cloudIn;
    public int position;
    public SQLSong song;
}
