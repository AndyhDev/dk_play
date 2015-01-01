package com.dk.play.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.service.PlayService;
import com.dk.play.util.SearchListener;
import com.dk.play.util.SearchPlayable;
import com.dk.play.util.SongAdapter;

public class SearchFragment extends Fragment {
	private SearchPlayable search;
	private ListView songView;
	private SQLSongList playlist;
	private SongAdapter songAdt;
	private ProgressBar spinner;
	private Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search, container, false);

		spinner = (ProgressBar)rootView.findViewById(R.id.progressBar1);
		spinner.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.dark_red), android.graphics.PorterDuff.Mode.SRC_IN);
		
		playlist = new SQLSongList();
		songView = (ListView)rootView.findViewById(R.id.listView1);
		songAdt = new SongAdapter(this.getActivity(), playlist);
		songView.setAdapter(songAdt);
		
		start();
		return rootView;
	}
	public void start(){
		playlist = new SQLSongList();
		songAdt.setSongList(playlist);
		songAdt.notifyDataSetChanged();
		
		search = new SearchPlayable(this.getActivity());
		search.setSearchListener(listener);
		search.search();
		spinner.setIndeterminate(true);
	}
	private SearchListener listener = new SearchListener() {

		@Override
		public void onStart() {
		}

		@Override
		public void onFound(SQLSong song) {
			playlist.add(0, song);
			songAdt.setSongList(playlist);
			songAdt.notifyDataSetChanged();

		}

		@Override
		public void onEnd() {
			Intent readNew = new Intent(PlayService.READ_NEW);
			getActivity().sendBroadcast(readNew);
			
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					spinner.setIndeterminate(false);
					try {
						Toast.makeText(getActivity(), R.string.search_end, Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			};
			handler.postDelayed(runnable, 4000);

		}
	};
	/*private void finish(){
		spinner.setIndeterminate(false);
		this.getActivity().finish();
	}*/
}