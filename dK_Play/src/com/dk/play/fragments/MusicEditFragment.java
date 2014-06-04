package com.dk.play.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.VideoEditActivity;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;

public class MusicEditFragment extends Fragment{
	@SuppressWarnings("unused")
	private static final String TAG = "MusicEditFragment";

	private long id;
	@SuppressWarnings("unused")
	private Context context;
	private ScrollView layout;
	private ImageView coverImage;
	private SQLSong song;
	private EditText title;
	private EditText artist;
	private RatingBar rating;
	private Spinner genre;
	private int imgSize;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ScrollView) inflater.inflate(R.layout.music_edit_fragment, container, false);
		context = this.getActivity();

		Intent intent = this.getActivity().getIntent();
		id = intent.getLongExtra(VideoEditActivity.KEY_ID, 0);

		imgSize = getPx(80);

		SQLiteDataSource datasource = new SQLiteDataSource(getActivity());
		datasource.open();
		song = datasource.getSQLSongList().getById(id);
		datasource.close();

		coverImage = (ImageView)layout.findViewById(R.id.cover_image);
		coverImage.setImageBitmap(song.getCoverBitmap(imgSize));
		
		title = (EditText)layout.findViewById(R.id.title_edit);
		title.setText(song.getTitle());
		title.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				title.setError(null);
			}
		}); 


		artist = (EditText)layout.findViewById(R.id.artist_edit);
		artist.setText(song.getArtist());

		rating = (RatingBar)layout.findViewById(R.id.rating_edit);
		rating.setRating(song.getRating());

		genre = (Spinner)layout.findViewById(R.id.genre_edit);
		makeGenre();

		return layout;
	}

	private int getPx(int dpi){
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
		return (int)px;
	}

	@SuppressWarnings("unchecked")
	private void makeGenre(){
		if(song.getGenre() != null){
			ArrayAdapter<String> adapter = (ArrayAdapter<String>) genre.getAdapter();
			int pos = adapter.getPosition(song.getGenre());
			if(pos >= 0){
				genre.setSelection(pos);
			}
			
		}
	}
	@SuppressWarnings("unchecked")
	public void save(){
		if(title.getText().toString().length() == 0){
			title.setError(getResources().getString(R.string.insert_title));
			return;
		}
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) genre.getAdapter();
		int pos = genre.getSelectedItemPosition();
		String g = adapter.getItem(pos);

		song.setTitle(title.getText().toString());
		song.setArtist(artist.getText().toString());
		song.setRating((int) rating.getRating());
		song.setGenre(g);

		SQLiteDataSource datasource = new SQLiteDataSource(getActivity());
		datasource.open();
		datasource.updateSong(song);
		datasource.close();

		Intent readNew = new Intent(PlayService.READ_NEW);
		this.getActivity().sendBroadcast(readNew);

		Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_LONG).show();
		this.getActivity().finish();
	}
}
