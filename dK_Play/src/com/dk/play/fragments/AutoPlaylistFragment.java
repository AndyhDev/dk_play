package com.dk.play.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.dk.play.R;
import com.dk.play.SpecialPlaylist;

public class AutoPlaylistFragment extends Fragment implements OnClickListener {
	private Context context;
	private ScrollView layout;
	private RelativeLayout playlistMain;
	private RelativeLayout playlistRandom;
	private RelativeLayout playlistMost;
	private RelativeLayout playlistClick;
	private RelativeLayout playlistDate;
	private RelativeLayout playlistR5;
	private RelativeLayout playlistR4;
	private RelativeLayout playlistR3;
	private RelativeLayout playlistR2;
	private RelativeLayout playlistR1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		context = this.getActivity();
		layout = (ScrollView)inflater.inflate(R.layout.auto_playlist_fragment, container, false);
		playlistMain = (RelativeLayout)layout.findViewById(R.id.playlist_main);
		playlistMain.setOnClickListener(this);
		
		playlistRandom = (RelativeLayout)layout.findViewById(R.id.playlist_random);
		playlistRandom.setOnClickListener(this);
		
		playlistMost = (RelativeLayout)layout.findViewById(R.id.playlist_most);
		playlistMost.setOnClickListener(this);
		
		playlistClick = (RelativeLayout)layout.findViewById(R.id.playlist_click);
		playlistClick.setOnClickListener(this);
		
		playlistDate = (RelativeLayout)layout.findViewById(R.id.playlist_date);
		playlistDate.setOnClickListener(this);
		
		playlistR5 = (RelativeLayout)layout.findViewById(R.id.playlist_r5);
		playlistR5.setOnClickListener(this);
		
		playlistR4 = (RelativeLayout)layout.findViewById(R.id.playlist_r4);
		playlistR4.setOnClickListener(this);
		
		playlistR3 = (RelativeLayout)layout.findViewById(R.id.playlist_r3);
		playlistR3.setOnClickListener(this);
		
		playlistR2 = (RelativeLayout)layout.findViewById(R.id.playlist_r2);
		playlistR2.setOnClickListener(this);
		
		playlistR1 = (RelativeLayout)layout.findViewById(R.id.playlist_r1);
		playlistR1.setOnClickListener(this);
		
		return layout;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = new Intent(context, SpecialPlaylist.class);
		intent.setAction(SpecialPlaylist.PLAY);
		intent.putExtra(SpecialPlaylist.PLAYLIST_ID, id);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
