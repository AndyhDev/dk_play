package com.dk.play.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.dk.play.DkPlay;
import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.util.Image;
import com.dk.play.util.Paths;
import com.dk.play.widget.WidgetBig2Receiver;
import com.dk.play.widget.WidgetBigReceiver;

public class PlayService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
	private final IBinder binder = new MyBinder();
	public static final String TAG = "PlayService";

	public static final int LOOP_STATE_NO_LOOP = 0;
	public static final int LOOP_STATE_LOOP = 1;
	public static final int LOOP_STATE_LOOP_SONG = 2;
	public static final int LOOP_STATE_SHUFFLE = 3;

	public static final String NEW_SONG = "com.dk.play.service.NEW_SONG";
	public static final String NEW_LOOP = "com.dk.play.service.NEW_LOOP";
	public static final String NEW_PLAY_STATE = "com.dk.play.service.NEW_PLAY_STATE";
	public static final String READ_NEW = "com.dk.play.service.READ_NEW";
	public static final String NEW_PLAYLIST = "com.dk.play.service.NEW_PLAYLIST";

	public static final String SONG_ID = "com.dk.play.service.SONG_ID";
	public static final String SONG_TITLE = "com.dk.play.service.SONG_TITLE";
	public static final String SONG_ARTIST = "com.dk.play.service.SONG_ARTIST";
	public static final String SONG_ALBUM = "com.dk.play.service.SONG_ALBUM";
	public static final String SONG_GENRE = "com.dk.play.service.SONG_GENRE";
	public static final String SONG_COVER = "com.dk.play.service.SONG_COVER";
	public static final String SONG_RATING = "com.dk.play.service.SONG_RATING";
	public static final String SONG_PLAY_COUNT = "com.dk.play.service.SONG_PLAY_COUNT";
	public static final String SONG_PATH = "com.dk.play.service.SONG_PATH";
	public static final String SONG_TIME = "com.dk.play.service.SONG_TIME";
	public static final String SONG_TYPE = "com.dk.play.service.SONG_TYPE";
	public static final String SONG_INDEX = "com.dk.play.service.SONG_INDEX";

	public static final String LOOP_STATE = "com.dk.play.service.LOOP_STATE";

	public static final String PLAY_STATE = "com.dk.play.service.PLAY_STATE";

	public static final int PLAY_STATE_PLAY = 6;
	public static final int PLAY_STATE_PAUSE = 7;
	public static final int PLAY_STATE_STOP = 8;

	public static final String PLAYLIST_MODIFY = "com.dk.play.service.PLAYLIST_MODIFY";
	public static final String PLAYLIST_NAME = "com.dk.play.service.PLAYLIST_NAME";
	public static final String PLAYLIST_ID = "com.dk.play.service.PLAYLIST_ID";
	public static final String PLAYLIST_CREATED = "com.dk.play.service.PLAYLIST_CREATED";
	public static final String PLAYLIST_DELETED = "com.dk.play.service.PLAYLIST_DELETED";
	
	public static final String WIDGET_SONG_ID = "SONG_ID";
	
	private SQLiteDataSource datasource;
	private SQLSongList songList;
	private SQLSong curSong = null;;
	private int curPos;
	private MediaPlayer player;
	private int loopState;
	private int playState = PLAY_STATE_STOP;
	private SharedPreferences settings;
	private Boolean playAtBoot;
	private Boolean pause = false;
	private Integer curDuration;
	private static final int notifyId = 1;
	private RemoteControlClient remoteControlClient;
	private LockScreen lockScreenReceiver;
	private RemoteControl remoteControlReceiver;
	private PlaylistReceiver playlistReceiver;
	private String playlistName;
	private String startSong;
	private Boolean started;
	
	public void onCreate(){
		started = false;
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(settingsListener);

		playAtBoot = settings.getBoolean("play_at_boot", false);
		startSong = settings.getString("start_song", "null");
		loopState = Integer.parseInt(settings.getString("loop_modus", "1"));
		Log.d(TAG, "playAtBoot:" + playAtBoot);
		Log.d(TAG, "startSong:" + startSong);
		Log.d(TAG, "loopState:" + loopState);

		if(lockScreenReceiver == null){
			lockScreenReceiver = new LockScreen();

			IntentFilter intentFilter = new IntentFilter(LockScreenReceiver.LOCK_SCREEN_ACTION);
			registerReceiver(lockScreenReceiver, intentFilter);
		}

		if(remoteControlReceiver == null){
			remoteControlReceiver = new RemoteControl();

			IntentFilter intentFilter = new IntentFilter(RemoteControl.REMOTE_CONTROL);
			registerReceiver(remoteControlReceiver, intentFilter);
		}

		if(playlistReceiver == null){
			playlistReceiver = new PlaylistReceiver();

			IntentFilter intentFilter = new IntentFilter(PLAYLIST_MODIFY);
			registerReceiver(playlistReceiver, intentFilter);
		}

		ComponentName myEventReceiver = new ComponentName(getPackageName(), LockScreenReceiver.class.getName());
		AudioManager myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.registerMediaButtonEventReceiver(myEventReceiver);

		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(myEventReceiver);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);

		remoteControlClient = new RemoteControlClient(mediaPendingIntent);
		remoteControlClient.setTransportControlFlags(
				RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				RemoteControlClient.FLAG_KEY_MEDIA_NEXT);

		myAudioManager.registerRemoteControlClient(remoteControlClient);
		myAudioManager.requestAudioFocus(focusChangeListener,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		readSongList();
		if(player != null){
			player.reset();
		}
		player = new MediaPlayer();
		initMusicPlayer();
		if(startSong == null){
			Log.d(TAG, "startSong == null");
		}
		if(!setStartSong(startSong)){
			setSongByPos(0, false);
		}
	}
	private Boolean setStartSong(String sId) {
		try {
			if(sId == "null"){
				return false;
			}
			Long id = Long.valueOf(sId);
			setSongById(id, false);
			return true;
		} catch (Exception e){
			Log.d(TAG, "error while setting startSong");
		}
		return false;

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand:" + startId);
		if(intent != null){
			if(intent.getAction() != null){
				if(intent.getAction().equals(BootBroadcastReceiver.SERVICE_ACTION)){
					if(playAtBoot){
						play();
					}
				}else if(intent.getAction().equals(RemoteControlReceiver.ACTION_START) && started != true){
					Long songId = intent.getLongExtra(WIDGET_SONG_ID, 0);
					setSongById(songId);
					
					String action = intent.getStringExtra(RemoteControl.ACTION);
					
					if(action.equals(RemoteControl.ACTION_NEXT)){
						next();
					}else if(action.equals(RemoteControl.ACTION_PREV)){
						prev();
					}else if(action.equals(RemoteControl.ACTION_STOP)){
						stop();
					}else if(action.equals(RemoteControl.ACTION_PLAY)){
						play();
					}else if(action.equals(RemoteControl.ACTION_PAUSE)){
						pause();
					}else if(action.equals(RemoteControl.ACTION_PLAY_PAUSE)){
						if(isPlaying()){
							pause();
						}else{
							play();
						}
					}else if(action.equals(RemoteControl.ACTION_CLOSE)){
						stop();
						stopNotify();
					}else if(action.equals(RemoteControl.ACTION_LOOP)){
						loop();
					}
				}
			}
		}
		if(!started){
			started = true;
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	@Override
	public boolean onUnbind(Intent intent){
		return true;
	}
	public class MyBinder extends Binder {
		public PlayService getService() {
			return PlayService.this;
		}
	}
	SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			playAtBoot = prefs.getBoolean("play_at_boot", false);
			loopState = Integer.parseInt(prefs.getString("loop_modus", "1"));
			startSong = settings.getString("start_song", "0");
		}
	};
	private void initMusicPlayer(){
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	private void readSongList(){
		datasource = new SQLiteDataSource(this);
		datasource.open();
		songList = datasource.getSQLSongList();
		datasource.close();
		playlistName = "main";
	}
	public void setMainList(){
		readSongList();
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public void setSongById(long id){
		setSong(id, true);
	}
	public void setSongById(long id, Boolean playing){
		setSong(id, playing);
	}
	public void setSongByPos(int pos){
		SQLSong song = songList.get(pos);
		if(song != null){
			setSong(song.getId(), true);

		}
	}
	public void setSongByPos(int pos, Boolean playing){
		SQLSong song = songList.get(pos);
		if(song != null){
			setSong(song.getId(), playing);

		}
	}
	public SQLSongList getSQLSongList(){
		return songList.copy();
	}
	public void setSQLSongList(SQLSongList list){
		songList = list.copy();
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public void moveSong(int from, int to){
		songList.moveSong(from, to);
		Log.d(TAG, "moveSong("+from+", "+to+")");
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public String getPlayListName(){
		return playlistName;
	}
	public void setPlayListName(String playlistName){
		this.playlistName = playlistName;
	}
	public Boolean isSongIn(long id){
		SQLSong song = songList.getById(id);
		if(song == null){
			return false;
		}
		return true;
	}
	public void setSong(long id, Boolean playing){
		SQLSong song = songList.getById(id);
		if(song != null){
			curSong = song;
			song.playcountUp();

			SQLiteDataSource db = new SQLiteDataSource(this);
			db.open();
			db.updateSong(song);
			db.close();

			curPos = songList.getIndex(song);
			updateMetadata();
			if(playing){
				play();
			}else{
				sendCurSong();
			}
		}
		updateWidgets();
	}
	public void processClick(SQLSong song){
		song.click();

		SQLiteDataSource db = new SQLiteDataSource(this);
		db.open();
		db.updateSong(song);
		db.close();
	}
	public SQLSong getCurSong(){
		return curSong;
	}
	public int getSongIndex(){
		if(songList.size() == 0){
			return 0;
		}
		return songList.getIndex(curSong);
	}
	public String echo(String test){
		return test;
	}
	private void checkLoop(){
		if(songList != null){
			if(loopState == LOOP_STATE_NO_LOOP){
				return;
			}else if(loopState == LOOP_STATE_LOOP){
				next();
			}else if(loopState == LOOP_STATE_LOOP_SONG){
				play();
			}else if(loopState == LOOP_STATE_SHUFFLE){
				setSongByPos(randIndex());
				play();

				return;
			}
		}
	}
	private int randIndex(){
		if(songList == null){
			return 0;
		}
		int low = 0;
		int high = songList.size() - 1;
		return (int) (Math.random() * (high - low) + low);
	}
	public void play(){
		if(curSong == null){
			return;
		}
		if(pause){
			player.start();
			pause = false;
			playState = PLAY_STATE_PLAY;
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			startNotify();
			updateWidgets();
			Intent newLoop = new Intent(NEW_PLAY_STATE);
			newLoop.putExtra(PLAY_STATE, PLAY_STATE_PLAY);
			sendBroadcast(newLoop);
		}else{
			player.reset();
			try {
				player.setDataSource(curSong.getPath());
				player.prepareAsync();
			}catch (IOException e){
				e.printStackTrace();
				final SQLSong song = new SQLSong(curSong);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.medium_corrupt));
				builder.setIcon(R.drawable.ic_launcher);
				builder.setMessage(getString(R.string.medium_corrupt_long, song.getPath()));

				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton){
						if(songList.isIn(song)){
							removeSong(song);
						}
						datasource = new SQLiteDataSource(PlayService.this);
						datasource.open();
						datasource.removeSQLSong(song);
						datasource.close();

						Intent readNew = new Intent(READ_NEW);
						sendBroadcast(readNew);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alert.show();
				next();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	public Boolean isPlaying(){
		return player.isPlaying();
	}
	public void pause(){
		if(player.isPlaying()){
			player.pause();
			pause = true;
			startNotify();
			playState = PLAY_STATE_PAUSE;
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
			updateWidgets();
			Intent newLoop = new Intent(NEW_PLAY_STATE);
			newLoop.putExtra(PLAY_STATE, PLAY_STATE_PAUSE);
			sendBroadcast(newLoop);
		}

	}
	public void stop(){
		if(player.isPlaying()){
			player.stop();

		}
		player.reset();
		playState = PLAY_STATE_STOP;
		pause = false;
		stopNotify();
		remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		updateWidgets();
		Intent newLoop = new Intent(NEW_PLAY_STATE);
		newLoop.putExtra(PLAY_STATE, PLAY_STATE_STOP);
		sendBroadcast(newLoop);
	}
	public void loop(){
		if(loopState == LOOP_STATE_NO_LOOP){
			loopState = LOOP_STATE_LOOP;
		}else if(loopState == LOOP_STATE_LOOP){
			loopState = LOOP_STATE_LOOP_SONG;
		}else if(loopState == LOOP_STATE_LOOP_SONG){
			loopState = LOOP_STATE_SHUFFLE;
		}else if(loopState == LOOP_STATE_SHUFFLE){
			loopState = LOOP_STATE_NO_LOOP;
		}
		Log.d(TAG, "loopState=" + loopState);
		Intent newLoop = new Intent(NEW_LOOP);
		newLoop.putExtra(LOOP_STATE, loopState);
		sendBroadcast(newLoop);
		updateWidgets();
	}
	public int getPlayState(){
		return playState;
	}
	public int getLoopState(){
		return loopState;
	}
	public int getPosition(){
		if(player.isPlaying()){
			return player.getCurrentPosition();
		}
		return -1;
	}
	public int getPosition(int max){
		float max2 = max;
		if(player.isPlaying() && curDuration != null){
			return (int)((max2 / curDuration) * player.getCurrentPosition());
		}
		return -1;
	}
	public int getDuration(){
		if(player.isPlaying()){
			return player.getDuration();
		}
		return -1;
	}
	public void jumpTo(int gap, int max){
		float max2 = max;
		if(player.isPlaying() && curDuration != null){
			Log.d(TAG, "seekTo:" + (int)((curDuration / max2) * gap));
			player.seekTo((int)((curDuration / max2) * gap));
		}
	}
	public void jumpTo(int mills){
		if(player.isPlaying()){
			player.seekTo(mills);
		}
	}
	public void next(){
		if(pause){
			player.reset();
			pause = false;
		}
		curPos = getSongIndex();
		Log.d(TAG, "index:" + curPos);
		curPos++;
		Log.d(TAG, "size2:"+ songList.size());
		if(curPos >= songList.size()){
			curPos = 0;
		}
		setSongByPos(curPos);
	}
	public void prev(){
		if(pause){
			player.reset();
			pause = false;
		}
		curPos = getSongIndex();
		curPos--;
		if(curPos < 0){
			curPos = songList.size() - 1;
		}
		setSongByPos(curPos);
	}
	public void addSong(SQLSong song){
		songList.add(song);
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public void addSong(int index, SQLSong song){
		songList.add(index, song);
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public void removeSong(SQLSong song){
		songList.remove(song);
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	public void removeSong(int index){
		songList.remove(index);
		Intent newLoop = new Intent(NEW_PLAYLIST);
		sendBroadcast(newLoop);
	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		checkLoop();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "onError");
		mp.reset();
		playState = PLAY_STATE_STOP;
		stopNotify();
		remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		updateWidgets();
		Intent newLoop = new Intent(NEW_PLAY_STATE);
		newLoop.putExtra(PLAY_STATE, PLAY_STATE_STOP);
		sendBroadcast(newLoop);
		//Toast.makeText(this, "cool", Toast.LENGTH_LONG).show();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp){
		mp.start();
		curDuration = mp.getDuration();
		pause = false;
		sendCurSong();
		playState = PLAY_STATE_PLAY;
		startNotify();
		remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		updateWidgets();
		Intent newLoop = new Intent(NEW_PLAY_STATE);
		newLoop.putExtra(PLAY_STATE, PLAY_STATE_PLAY);
		sendBroadcast(newLoop);
	}

	private void sendCurSong(){
		Intent newSong = new Intent(NEW_SONG);
		newSong.putExtra(SONG_ID, curSong.getId());
		newSong.putExtra(SONG_TITLE, curSong.getTitle());
		newSong.putExtra(SONG_ARTIST, curSong.getArtist());
		newSong.putExtra(SONG_ALBUM, curSong.getAlbum());
		newSong.putExtra(SONG_GENRE, curSong.getGenre());
		newSong.putExtra(SONG_COVER, curSong.getCover());
		newSong.putExtra(SONG_RATING, curSong.getRating());
		newSong.putExtra(SONG_PLAY_COUNT, curSong.getPlay_count());
		newSong.putExtra(SONG_PATH, curSong.getPath());
		newSong.putExtra(SONG_TIME, curSong.getTime());
		newSong.putExtra(SONG_TYPE, curSong.getType());
		newSong.putExtra(SONG_INDEX, songList.getIndex(curSong));
		sendBroadcast(newSong);
	}
	public void setRating(int rating) {
		if(curSong != null){
			curSong.setRating(rating);
			SQLiteDataSource db = new SQLiteDataSource(this);
			db.open();
			db.updateSong(curSong);
			db.close();

			Intent readNew = new Intent(READ_NEW);
			sendBroadcast(readNew);
		}
	}
	private void startNotify(){
		int curApi = android.os.Build.VERSION.SDK_INT;
		if(curApi >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			startNotifyApi16();
		}else{
			startNotifyApi14();
		}
	}

	private void startNotifyApi14(){
		Bitmap icon;
		String coverName = curSong.getCover();
		File cover = Paths.getCoverFile(coverName);
		if(cover.exists()){
			icon = Image.decodeSampledBitmapFromPath(cover.getAbsolutePath(), 200, 200);
		}else{
			icon = Image.decodeSampledBitmapFromResource(getResources(), R.drawable.default_cover, 200, 200);
		}
		Intent contentIntent = new Intent(this, DkPlay.class);
		PendingIntent pContentIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);

		Intent prevIntent = new Intent(this, RemoteControlReceiver.class);
		prevIntent.setAction(RemoteControl.ACTION_PREV);
		PendingIntent pPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent playIntent = new Intent(this, RemoteControlReceiver.class);
		playIntent.setAction(RemoteControl.ACTION_PLAY);
		PendingIntent pPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent nextIntent = new Intent(this, RemoteControlReceiver.class);
		nextIntent.setAction(RemoteControl.ACTION_NEXT);
		PendingIntent pNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent pauseIntent = new Intent(this, RemoteControlReceiver.class);
		pauseIntent.setAction(RemoteControl.ACTION_PAUSE);
		PendingIntent pPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_stat_notify);
		builder.setLargeIcon(icon);
		builder.setContentText(curSong.getTitle());
		builder.setContentTitle(getString(R.string.app_name));
		builder.setContentIntent(pContentIntent);
		builder.addAction(R.drawable.ic_action_previous, "", pPrevIntent);

		if(isPlaying()){
			builder.addAction(R.drawable.ic_action_pause, "", pPauseIntent);
		}else{
			builder.addAction(R.drawable.ic_action_play, "", pPlayIntent);
		}

		builder.addAction(R.drawable.ic_action_next, "", pNextIntent);


		Notification n = builder.build();


		startForeground(notifyId, n);
	}

	@SuppressLint("NewApi")
	private void startNotifyApi16(){
		Bitmap icon;
		String coverName = curSong.getCover();
		File cover = Paths.getCoverFile(coverName);
		if(cover.exists()){
			icon = Image.decodeSquareBitmapFromPath(cover.getAbsolutePath(), 200);
		}else{
			icon = Image.decodeSampledBitmapFromResource(getResources(), R.drawable.default_cover, 200, 200);
		}
		//Bitmap logo = Image.decodeSampledBitmapFromResource(getResources(), R.drawable.ic_launcher, 100, 100);

		Intent contentIntent = new Intent(this, DkPlay.class);
		PendingIntent pContentIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);

		Intent prevIntent = new Intent(this, RemoteControlReceiver.class);
		prevIntent.setAction(RemoteControl.ACTION_PREV);
		PendingIntent pPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent playIntent = new Intent(this, RemoteControlReceiver.class);
		playIntent.setAction(RemoteControl.ACTION_PLAY);
		PendingIntent pPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		/*Intent stopIntent = new Intent(this, RemoteControlReceiver.class);
		stopIntent.setAction(RemoteControl.ACTION_STOP);
		PendingIntent pStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);*/

		Intent nextIntent = new Intent(this, RemoteControlReceiver.class);
		nextIntent.setAction(RemoteControl.ACTION_NEXT);
		PendingIntent pNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent pauseIntent = new Intent(this, RemoteControlReceiver.class);
		pauseIntent.setAction(RemoteControl.ACTION_PAUSE);
		PendingIntent pPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent closeIntent = new Intent(this, RemoteControlReceiver.class);
		closeIntent.setAction(RemoteControl.ACTION_CLOSE);
		PendingIntent pCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Builder builder;
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.small_notify);
		remoteViews.setOnClickPendingIntent(R.id.next, pNextIntent);
		remoteViews.setOnClickPendingIntent(R.id.close, pCloseIntent);
		remoteViews.setImageViewBitmap(R.id.cover, icon);
		remoteViews.setTextViewText(R.id.title1, curSong.getTitle());

		RemoteViews remoteViews2 = new RemoteViews(getPackageName(), R.layout.big_notify);
		remoteViews2.setOnClickPendingIntent(R.id.prev, pPrevIntent);
		remoteViews2.setOnClickPendingIntent(R.id.next, pNextIntent);
		remoteViews2.setOnClickPendingIntent(R.id.close, pCloseIntent);
		remoteViews2.setImageViewBitmap(R.id.cover, icon);
		remoteViews2.setTextViewText(R.id.title, curSong.getTitle());
		remoteViews2.setTextViewText(R.id.artist, curSong.getArtist());

		if(isPlaying()){
			remoteViews.setOnClickPendingIntent(R.id.pause, pPauseIntent);
			remoteViews2.setOnClickPendingIntent(R.id.pause, pPauseIntent);
			remoteViews.setImageViewResource(R.id.pause, R.drawable.ic_action_pause);
			remoteViews2.setImageViewResource(R.id.pause, R.drawable.ic_action_pause);
		}else{
			remoteViews.setImageViewResource(R.id.pause, R.drawable.ic_action_play);
			remoteViews2.setImageViewResource(R.id.pause, R.drawable.ic_action_play);

			remoteViews.setOnClickPendingIntent(R.id.pause, pPlayIntent);
			remoteViews2.setOnClickPendingIntent(R.id.pause, pPlayIntent);
		}
		builder  = new Notification.Builder(this);
		builder.setContent(remoteViews); 
		builder.setSmallIcon(R.drawable.ic_stat_notify);
		builder.setContentIntent(pContentIntent);

		Notification n = builder.build();
		n.bigContentView = remoteViews2;

		startForeground(notifyId, n);
	}

	private void stopNotify(){
		stopForeground(true);
	}
	private void updateMetadata(){
		String artist = curSong.getArtist();
		String title = curSong.getTitle();
		String coverName = curSong.getCover();

		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, artist);

		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title);

		File img = Paths.getCoverFile(coverName);
		if(img.exists()){
			Bitmap coverArt = BitmapFactory.decodeFile(img.getAbsolutePath());
			metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, coverArt);
		}else{
			Bitmap coverArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
			metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, coverArt);
		}
		metadataEditor.apply();
	}
	private void updateWidgets(){
		RemoteViews view;
		List<Integer> layouts = new ArrayList<Integer>();
		layouts.add(R.layout.widget_big);
		layouts.add(R.layout.widget2_big);
		for(int i = 0; i < layouts.size(); i++){
			int layout = layouts.get(i);

			view = new RemoteViews(getPackageName(), layout);
			if(curSong != null){
				Bitmap cover = curSong.getCoverBitmap(250);
				view.setImageViewBitmap(R.id.cover, cover);
				view.setTextViewText(R.id.title, curSong.getTitle());
			}

			int srcId = R.drawable.loop;
			if(loopState == PlayService.LOOP_STATE_NO_LOOP){
				srcId = R.drawable.loop;
			}else if(loopState == PlayService.LOOP_STATE_LOOP){
				srcId = R.drawable.loop1;
			}else if(loopState == PlayService.LOOP_STATE_LOOP_SONG){
				srcId = R.drawable.loop2;
			}else if(loopState == PlayService.LOOP_STATE_SHUFFLE){
				srcId = R.drawable.ic_action_shuffle;
			}
			view.setImageViewResource(R.id.loop, srcId);

			Intent nextIntent = new Intent(this, RemoteControlReceiver.class);
			nextIntent.setAction(RemoteControl.ACTION_NEXT);
			if(curSong != null){
				nextIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
			}
			PendingIntent pNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent pauseIntent = new Intent(this, RemoteControlReceiver.class);
			pauseIntent.setAction(RemoteControl.ACTION_PAUSE);
			if(curSong != null){
				pauseIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
			}
			PendingIntent pPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent prevIntent = new Intent(this, RemoteControlReceiver.class);
			prevIntent.setAction(RemoteControl.ACTION_PREV);
			if(curSong != null){
				prevIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
			}
			PendingIntent pPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Intent loopIntent = new Intent(this, RemoteControlReceiver.class);
			loopIntent.setAction(RemoteControl.ACTION_LOOP);
			if(curSong != null){
				loopIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
			}
			PendingIntent pLoopIntent = PendingIntent.getBroadcast(this, 0, loopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			view.setOnClickPendingIntent(R.id.prev, pPrevIntent);
			view.setOnClickPendingIntent(R.id.pause, pPauseIntent);
			view.setOnClickPendingIntent(R.id.next, pNextIntent);
			view.setOnClickPendingIntent(R.id.loop, pLoopIntent);

			if(!isPlaying()){
				Log.d(TAG, "stop");
				Intent playIntent = new Intent(this, RemoteControlReceiver.class);
				playIntent.setAction(RemoteControl.ACTION_PLAY);
				if(curSong != null){
					playIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
				}
				PendingIntent pPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				view.setOnClickPendingIntent(R.id.play, pPlayIntent);
				view.setImageViewResource(R.id.play, R.drawable.ic_action_play);
			}else{
				Log.d(TAG, "stop");
				Intent stopIntent = new Intent(this, RemoteControlReceiver.class);
				stopIntent.setAction(RemoteControl.ACTION_STOP);
				if(curSong != null){
					stopIntent.putExtra(WIDGET_SONG_ID, curSong.getId());
				}
				PendingIntent pStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				view.setOnClickPendingIntent(R.id.play, pStopIntent);
				view.setImageViewResource(R.id.play, R.drawable.ic_action_stop);
			}
			ComponentName thisWidget;
			if(layout == R.layout.widget_big){
				thisWidget = new ComponentName(this, WidgetBigReceiver.class);
			}else{
				thisWidget = new ComponentName(this, WidgetBig2Receiver.class);
			}
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, view);

		}
	}
	public class LockScreen extends BroadcastReceiver{
		private long lastEvent = 0;

		@Override
		public void onReceive(Context context, Intent intent) {
			if(LockScreenReceiver.LOCK_SCREEN_ACTION.equals(intent.getAction())){
				long diff = System.currentTimeMillis() - lastEvent;
				Log.d(TAG, "diff:" + diff);
				if(System.currentTimeMillis() - lastEvent > 900){
					lastEvent = System.currentTimeMillis();
				}else{
					return;
				}

				String action = intent.getStringExtra(LockScreenReceiver.ACTION);

				if(action.equals(LockScreenReceiver.ACTION_NEXT)){
					next();
				}else if(action.equals(LockScreenReceiver.ACTION_PREV)){
					prev();
				}else if(action.equals(LockScreenReceiver.ACTION_STOP)){
					stop();
				}else if(action.equals(LockScreenReceiver.ACTION_PLAY)){
					play();
				}else if(action.equals(LockScreenReceiver.ACTION_PAUSE)){
					pause();
				}else if(action.equals(LockScreenReceiver.ACTION_PLAY_PAUSE)){
					if(isPlaying()){
						pause();
					}else{
						play();
					}
				}
			}
		}
	}
	public class RemoteControl extends BroadcastReceiver{
		private static final String TAG = "RemoteControl";

		public static final String REMOTE_CONTROL = "REMOTE_CONTROL";
		public static final String ACTION = "ACTION";
		public static final String ACTION_PLAY = "ACTION_PLAY";
		public static final String ACTION_STOP = "ACTION_STOP";
		public static final String ACTION_PAUSE = "ACTION_PAUSE";
		public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
		public static final String ACTION_NEXT = "ACTION_NEXT";
		public static final String ACTION_PREV = "ACTION_PREV";
		public static final String ACTION_CLOSE = "ACTION_CLOSE";
		public static final String ACTION_LOOP = "ACTION_LOOP";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getStringExtra(ACTION);
			Log.d(TAG, "action=" + action);
			if(action.equals(ACTION_NEXT)){
				next();
			}else if(action.equals(ACTION_PREV)){
				prev();
			}else if(action.equals(ACTION_STOP)){
				stop();
			}else if(action.equals(ACTION_PLAY)){
				play();
			}else if(action.equals(ACTION_PAUSE)){
				pause();
			}else if(action.equals(ACTION_PLAY_PAUSE)){
				if(isPlaying()){
					pause();
				}else{
					play();
				}
			}else if(action.equals(ACTION_CLOSE)){
				stop();
				stopNotify();
			}else if(action.equals(ACTION_LOOP)){
				loop();
			}
		}
	}
	public class PlaylistReceiver extends BroadcastReceiver{
		private static final String TAG = "PlaylistReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			String name = intent.getStringExtra(PLAYLIST_NAME);
			Log.d(TAG, "playlist update=" + name);
			if(playlistName.equals(name)){
				datasource = new SQLiteDataSource(PlayService.this);
				datasource.open();
				songList = datasource.getSQLPlaylist(name).getSQLSongList(getApplicationContext());
				Log.d(TAG, "size:" + songList.size());
				datasource.close();
			}
		}
	}
	private OnAudioFocusChangeListener focusChangeListener = 
			new OnAudioFocusChangeListener() {

		public void onAudioFocusChange(int focusChange) {
			//AudioManager am =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		}
	};
}
