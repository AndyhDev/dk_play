package com.dk.play.fragments;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.VideoEditActivity;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;

public class VideoEditFragment extends Fragment implements OnClickListener{
	private static final String TAG = "VideoEditFragment";
	private static final String COVER_INDEX = "COVER_INDEX";
	private static final String IMAGES_LIST= "IMAGES_LIST";

	private long id;
	private Context context;
	private ScrollView layout;
	private LinearLayout coverList;
	private SQLSong song;
	private EditText title;
	private EditText artist;
	private RatingBar rating;
	private Spinner genre;
	private int imgSize;
	private int imgMargin;
	private Integer selectedIndex = 0;
	private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
	private ProgressBar loading;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ScrollView) inflater.inflate(R.layout.video_edit_fragment, container, false);
		context = this.getActivity();

		Intent intent = this.getActivity().getIntent();
		id = intent.getLongExtra(VideoEditActivity.KEY_ID, 0);

		imgSize = getPx(80);
		imgMargin = getPx(5);

		SQLiteDataSource datasource = new SQLiteDataSource(getActivity());
		datasource.open();
		song = datasource.getSQLSongList().getById(id);
		datasource.close();

		coverList = (LinearLayout)layout.findViewById(R.id.cover_list);
		loading = new ProgressBar(context);
		loading.setIndeterminate(true);
		coverList.addView(loading);
		
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

		if(savedInstanceState != null){
			selectedIndex = savedInstanceState.getInt(COVER_INDEX);
			images = savedInstanceState.getParcelableArrayList(IMAGES_LIST);
			Log.d(TAG, "SIZE2:" + images.size());
			readImagesList();
		}else{
			makeCoverList();
		}
		return layout;
	}

	private int getPx(int dpi){
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
		return (int)px;
	}
	private void readImagesList(){
		coverList.post(new Runnable() {
			public void run() {
				coverList.removeAllViews();
			}
		});
		Log.d(TAG, "11");
		for(int i = 0; i < images.size(); i++){
			if(selectedIndex == i){
				addImage(images.get(i), true);
			}else{
				addImage(images.get(i));
			}
			Log.d(TAG, "i=" + i);
		}
		if(selectedIndex == null){
			selectIndex(0);
		};
	}
	private void makeCoverList() {
		Log.d(TAG, "1");
		new Thread(new Runnable() {
			public void run() {
				Bitmap bmp = song.getCoverBitmap(80);
				images.add(bmp);
				//addImage(bmp, true);

				MediaMetadataRetriever id3 = new MediaMetadataRetriever();
				try{
					id3.setDataSource(song.getPath());
					String value = id3.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					if(value == null){
						Log.d(TAG, "2");
						setDefaultList();
						return;
					}
					long duration = (Long.parseLong(value) / 1000);


					if(duration > 10){
						Log.d(TAG, "duration:" + duration);
						for(int i = 0; i <= 10; i++){
							bmp = scaleBmp(id3.getFrameAtTime((duration/10) * (i*100000)));
							images.add(bmp);
							//addImage(bmp);
						}
					}else{
						bmp = scaleBmp(id3.getFrameAtTime());
						images.add(bmp);
						//addImage(bmp);
						Log.d(TAG, "3");
						Log.d(TAG, "duration:" + duration);
					}

				}catch(RuntimeException ex){
					setDefaultList();
				}
				selectedIndex = 0;
				readImagesList();
			}
		}).start();

	}
	private Bitmap scaleBmp(Bitmap bmp){
		int height = bmp.getHeight();
		int width = bmp.getWidth();
		int size = 0;
		int x = 0;
		int y = 0;
		if(height > width){
			size = width;
			y = (height - width) / 2; 
		}else{
			size = height;
			x = (width - height) / 2;
		}
		return Bitmap.createBitmap(bmp, x, y, size, size);
	}
	private void addImage(Bitmap bmp){
		addImage(bmp, false);
	}
	@SuppressWarnings("deprecation")
	private void addImage(Bitmap bmp, Boolean select){
		final ImageView img = new ImageView(context);
		img.setPadding(imgMargin, imgMargin, imgMargin, imgMargin);
		img.setClickable(true);
		img.setFocusable(true);
		img.setBackgroundDrawable(getResources().getDrawable(R.drawable.songlist_select_color));
		//ab api 16 nur noch ".setBackground"
		img.setOnClickListener(this);

		img.setImageBitmap(bmp);
		if(select){
			img.setBackgroundResource(R.color.dark_red);
		}
		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgSize, imgSize);
		layoutParams.setMargins(imgMargin, 0, imgMargin, 0);

		coverList.post(new Runnable() {
			public void run() {
				coverList.addView(img, layoutParams);
			}
		});
	}
	private void setDefaultList(){

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

		if(selectedIndex != 0 && selectedIndex != null){
			song.setCoverBitmap(images.get(selectedIndex));
		}
		SQLiteDataSource datasource = new SQLiteDataSource(getActivity());
		datasource.open();
		datasource.updateSong(song);
		datasource.close();

		Intent readNew = new Intent(PlayService.READ_NEW);
		this.getActivity().sendBroadcast(readNew);

		Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_LONG).show();
		this.getActivity().finish();
	}

	@Override
	public void onClick(View v) {
		int size = coverList.getChildCount();
		for(int i = 0; i < size; i++){
			View c = coverList.getChildAt(i);
			c.setBackgroundResource(R.color.white);
			if(c == v){
				selectedIndex = i;
			}
		}
		ImageView img = (ImageView)v;
		img.setBackgroundResource(R.color.dark_red);
	}
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(COVER_INDEX, getSelectedIndex());
		outState.putParcelableArrayList(IMAGES_LIST, images);
		Log.d(TAG, "SIZE:" + images.size());
		super.onSaveInstanceState(outState);
	}

	private void selectIndex(int i) {
		Log.d(TAG, "selectIndex(" + i + ")");
		final ImageView img = (ImageView)coverList.getChildAt(i);
		selectedIndex = i;
		if(img != null){
			img.post(new Runnable() {
				public void run() {
					img.setBackgroundResource(R.color.dark_red);
				}
			});
		}
	}

	private Integer getSelectedIndex() {
		return selectedIndex;
	}
}
