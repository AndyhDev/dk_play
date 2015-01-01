package com.dk.play.fragments;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.service.PlayService;

public class PlayerControlFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener, OnRatingBarChangeListener{
	private static final String TAG = "PlayerControlFragment";
	private RelativeLayout layout;
	private ImageView background;
	private TextView title;
	private Context context;
	private ViewGroup container;
	private ImageButton bnt;
	private ImageButton playBnt;
	private ImageButton pauseBnt;
	private ImageButton nextBnt;
	private ImageButton prevBnt;
	private ImageButton loopBnt;
	private ImageButton shareBnt;
	private RatingBar ratingBar;
	private SeekBar seekBar;
	private PlayService service;
	private ServiceReceiver serviceReceiver;
	private Boolean openState = false;
	private int closeSize = 70;
	private Handler seekBarHandler = new Handler();
	private Integer seekProgress;
	
	private boolean showExpander = true;
	
	private static final String KEY_OPEN_STATE = "openState";

	/*@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			openState = savedInstanceState.getBoolean(KEY_OPEN_STATE);
			Log.d(TAG, "savedInstanceState1 != null "+ openState);
		}else{
			Log.d(TAG, "savedInstanceState1 == null");
		}
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(savedInstanceState != null){
			openState = savedInstanceState.getBoolean(KEY_OPEN_STATE);
			Log.d(TAG, "savedInstanceState2 != null "+ openState);
		}else{
			Log.d(TAG, "savedInstanceState2 == null");
		}
		context = this.getActivity();

		this.container = container;
		if(container == null){
			Log.d(TAG, "container NULL");
		}
		layout = (RelativeLayout)inflater.inflate(R.layout.player_control, container, false);
		setUpUi();

		return layout;
	}
	private void setUpUi(){
		background = (ImageView)layout.findViewById(R.id.player_control_bg);
		title = (TextView)layout.findViewById(R.id.player_control_title);
		bnt = (ImageButton)layout.findViewById(R.id.player_control_expand);
		if(!showExpander){
			bnt.setVisibility(View.GONE);
		}
		playBnt = (ImageButton)layout.findViewById(R.id.player_control_play);
		pauseBnt = (ImageButton)layout.findViewById(R.id.player_control_pause);
		nextBnt = (ImageButton)layout.findViewById(R.id.player_control_next);
		prevBnt = (ImageButton)layout.findViewById(R.id.player_control_prev);
		loopBnt = (ImageButton)layout.findViewById(R.id.player_control_loop);
		shareBnt = (ImageButton)layout.findViewById(R.id.player_control_share);

		bnt.setOnClickListener(this);
		playBnt.setOnClickListener(this);
		pauseBnt.setOnClickListener(this);
		nextBnt.setOnClickListener(this);
		prevBnt.setOnClickListener(this);
		loopBnt.setOnClickListener(this);
		shareBnt.setOnClickListener(this);
		
		seekBar = (SeekBar)layout.findViewById(R.id.player_control_seekbar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBarHandler.postDelayed(seekBarUpdate, 100);

		ratingBar = (RatingBar)layout.findViewById(R.id.player_control_ratingbar);
		ratingBar.setOnRatingBarChangeListener(this);

		if(service != null){
			SQLSong song = service.getCurSong();
			setCover(song);
			setTitle(song);
			setLoop(service.getLoopState());
			setRating(song);
			setPlayState(service.getPlayState());
		}

	}
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if(service == null){
			Intent intent= new Intent(this.getActivity(), PlayService.class);
			context.startService(intent);
			context.bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
		}
		setOpenState();
		registerReceiver();
	}
	@Override
	public void onPause() {
		super.onPause();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
			serviceReceiver = null;
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
		}
	}
	private Runnable seekBarUpdate = new Runnable() {
		@Override
		public void run() {
			if(service != null){
				int pos = service.getPosition(1000);
				seekBar.setProgress(pos);
			}
			seekBarHandler.postDelayed(this, 100);
		}
	};
	private void registerReceiver(){
		if(serviceReceiver == null){
			serviceReceiver = new ServiceReceiver();

			IntentFilter intentFilter = new IntentFilter(PlayService.NEW_SONG);
			intentFilter.addAction(PlayService.NEW_LOOP);
			intentFilter.addAction(PlayService.NEW_PLAY_STATE);
			intentFilter.addAction(PlayService.OVERLAY_STARTED);
			this.getActivity().registerReceiver(serviceReceiver, intentFilter);
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
			SQLSong song = service.getCurSong();
			setCover(song);
			setTitle(song);
			setLoop(service.getLoopState());
			setRating(song);
			setPlayState(service.getPlayState());
		}

		public void onServiceDisconnected(ComponentName className) {
			service  = null;
		}
	};

	private class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "receive:" + intent.getAction());
			if (intent.getAction().equals(PlayService.NEW_SONG)) {
				Log.d(TAG, "new song:" + intent.getLongExtra(PlayService.SONG_ID, 0));
				setCover(intent.getStringExtra(PlayService.SONG_COVER));
				setTitle(intent.getStringExtra(PlayService.SONG_TITLE));
				setRating(intent.getIntExtra(PlayService.SONG_RATING, 0));
			}else if(intent.getAction().equals(PlayService.NEW_LOOP)){
				Log.d(TAG, "new loopState:" + intent.getIntExtra(PlayService.LOOP_STATE, 0));
				setLoop(intent.getIntExtra(PlayService.LOOP_STATE, PlayService.LOOP_STATE_NO_LOOP));
			}else if(intent.getAction().equals(PlayService.NEW_PLAY_STATE)){
				Log.d(TAG, "new playState:" + intent.getIntExtra(PlayService.PLAY_STATE, 0));
				setPlayState(intent.getIntExtra(PlayService.PLAY_STATE, PlayService.PLAY_STATE_STOP));
			}else if(intent.getAction().equals(PlayService.OVERLAY_STARTED)){
				getActivity().finish();
			}

		}
	}
	public void setPlayState(int playState){
		if(playState == PlayService.PLAY_STATE_PAUSE){
			playBnt.setImageResource(R.drawable.ic_action_play);
		}else if(playState == PlayService.PLAY_STATE_PLAY){
			playBnt.setImageResource(R.drawable.ic_action_stop);
		}else if(playState == PlayService.PLAY_STATE_STOP){
			playBnt.setImageResource(R.drawable.ic_action_play);
		}
	}
	public void setLoop(int loopState) {
		if(loopState == PlayService.LOOP_STATE_NO_LOOP){
			loopBnt.setImageResource(R.drawable.loop);
		}else if(loopState == PlayService.LOOP_STATE_LOOP){
			loopBnt.setImageResource(R.drawable.loop1);
		}else if(loopState == PlayService.LOOP_STATE_LOOP_SONG){
			loopBnt.setImageResource(R.drawable.loop2);
		}else if(loopState == PlayService.LOOP_STATE_SHUFFLE){
			loopBnt.setImageResource(R.drawable.ic_action_shuffle);
		}

	}
	private void setRating(SQLSong song){
		if(song != null){
			setRating(song.getRating());
		}
	}
	private void setRating(Integer rating){
		if(rating != null){
			ratingBar.setRating(rating);
		}
	}
	private void setTitle(SQLSong song){
		if(song == null){
			return;
		}
		setTitle(song.getTitle());
	}
	private void setTitle(String title){
		if(title == null){
			return;
		}
		this.title.setText(title);
		this.title.setSelected(true);
	}
	private void setCover(SQLSong song){
		if(song == null){
			return;
		}
		setCover(song.getCoverUri().getPath());
	}
	private void setCover(String coverPath){
		if(coverPath == null){
			return;
		}
		File cover = new File(coverPath);
		if(cover.exists()){
			Bitmap bmp = BitmapFactory.decodeFile(coverPath);
			background.setImageBitmap(bmp);
		}else{
			background.setImageDrawable(context.getResources().getDrawable(R.drawable.default_cover));
		}
	}
	private void setOpenState(){
		Log.d(TAG, "setOpenState");
		int height;
		int img;
		if(openState){
			setUiOpen();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			height = size.y;
			img = R.drawable.ic_action_expand;
		}else{
			setUiClose();
			height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, closeSize, getResources().getDisplayMetrics());
			img = R.drawable.ic_action_collapse;
		}
		LayoutParams params = (LayoutParams) container.getLayoutParams();
		params.height = height;
		container.setLayoutParams(params);
		bnt.setImageDrawable(getResources().getDrawable(img));
	}
	private void setUiOpen(){
		container.removeAllViews();
		layout = (RelativeLayout) View.inflate(context, R.layout.player_control_open, null);
		container.addView(layout);
		setUpUi();
	}
	private void setUiClose(){
		container.removeAllViews();
		layout = (RelativeLayout) View.inflate(context, R.layout.player_control, null);
		container.addView(layout);
		setUpUi();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.player_control_expand){
			startOpenAnimation();
		}else if(id == R.id.player_control_play){
			play();
		}else if(id == R.id.player_control_pause){
			pause();
		}else if(id == R.id.player_control_next){
			next();
		}else if(id == R.id.player_control_prev){
			prev();
		}else if(id == R.id.player_control_loop){
			loop();
		}else if(id == R.id.player_control_share){
			share();
		}
	}
	private void prev() {
		if(service != null){
			service.prev();
		}
	}
	private void next() {
		if(service != null){
			service.next();
		}
	}
	private void pause() {
		if(service != null){
			service.pause();
		}
	}
	private void play() {
		if(service != null){
			if(service.isPlaying()){
				service.stop();
			}else{
				service.play();
			}
		}
	}
	private void loop() {
		if(service != null){
			service.loop();
		}
	}
	private void share(){
		if(service != null){
			SQLSong song = service.getCurSong();
			if(song == null){
				return;
			}
			String body;
			if(song.getArtist().isEmpty()){
				body = getString(R.string.share_body2, song.getTitle());
			}else{
				body = getString(R.string.share_body1, song.getTitle(), song.getArtist());
			}
			
			body += " " + getString(R.string.share_body_end);
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			//Uri uri = song.getCoverUri();
			//if(uri != null){
			//	shareIntent.setType("*/*");
			//	shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			//}else{
			//	shareIntent.setType("text/plain");
			//}
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
			startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_title)));
		}
	}
	public void startOpenAnimation(){
		final int img;
		int miniHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, closeSize, getResources().getDisplayMetrics());
		LayoutParams params = (LayoutParams) container.getLayoutParams();

		final int newHeight;
		final int oldHeight = params.height;

		if(oldHeight != miniHeight){
			newHeight = miniHeight;
			img = R.drawable.ic_action_collapse;
			openState = false;
		}else{
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			newHeight = size.y;
			img = R.drawable.ic_action_expand;
			openState = true;
		}
		if(container == null){
			Log.d(TAG, "container NULL");
		}
		Animation a = new Animation() {

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				LayoutParams params = (LayoutParams) container.getLayoutParams();
				int h;
				if(oldHeight > newHeight){
					//zuklappen
					int diff = oldHeight - newHeight;
					h = oldHeight - ((int)(diff * interpolatedTime));
				}else{
					//aufklappen
					int diff = newHeight - oldHeight;
					h = oldHeight + ((int)(diff * interpolatedTime));
				}
				params.height = h;
				container.setLayoutParams(params);
			}
		};
		a.setDuration(300);
		a.setAnimationListener(new AnimationListener(){
			@SuppressLint("NewApi")
			@Override
			public void onAnimationEnd(Animation animation) {
				if(openState){
					setUiOpen();
				}else{
					setUiClose();
				}
				bnt.setImageDrawable(getResources().getDrawable(img));
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		container.startAnimation(a);
	}
	public Boolean getOpenState(){
		return openState;
	}
	public void setOpenState(Boolean openState){
		if(openState != this.openState){
			this.openState = openState;
			setOpenState();
		}
	}
	public Boolean trySetOpenState(Boolean openState){
		if(openState != this.openState){
			this.openState = openState;
			setOpenState();
			return true;
		}
		return false;
	}
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "save");
		outState.putBoolean(KEY_OPEN_STATE, openState);
		super.onSaveInstanceState(outState);
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser){
			seekProgress = progress;
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(seekProgress != null && service != null){
			service.jumpTo(seekProgress, 1000);
		}
		seekProgress = null;
	}
	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if(fromUser){
			if(service != null){
				service.setRating((int)rating);
			}
		}
	}
	
	public void setExpandable(boolean expandable){
		showExpander = expandable;
		if(!showExpander){
			if(bnt != null){
				bnt.setVisibility(View.GONE);
			}
		}
	}
	public boolean getExpandable(){
		return showExpander;
	}
	public void setPrevOpenState(boolean state){
		openState = state;
	}
}