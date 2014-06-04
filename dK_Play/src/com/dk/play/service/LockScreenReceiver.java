package com.dk.play.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class LockScreenReceiver extends BroadcastReceiver{
	private static final String TAG = "LockScreenReceiver";
	
	public static final String LOCK_SCREEN_ACTION = "LOCK_SCREEN_ACTION";
	public static final String ACTION = "ACTION";
	
	public static final String ACTION_PLAY = "ACTION_PLAY";
	public static final String ACTION_NEXT = "ACTION_NEXT";
	public static final String ACTION_PREV = "ACTION_PREV";
	public static final String ACTION_PAUSE = "ACTION_PAUSE";
	public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
	public static final String ACTION_STOP = "ACTION_STOP";
	
	private void sendPlay(Context context){
		//Log.d(TAG, "sendPlay");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_PLAY);
		context.sendBroadcast(intent);
	}
	private void sendNext(Context context){
		//Log.d(TAG, "sendNext");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_NEXT);
		context.sendBroadcast(intent);
	}
	private void sendPrev(Context context){
		//Log.d(TAG, "sendPrev");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_PREV);
		context.sendBroadcast(intent);
	}
	private void sendPause(Context context){
		//Log.d(TAG, "sendPause");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_PAUSE);
		context.sendBroadcast(intent);
	}
	private void sendPlayPause(Context context){
		//Log.d(TAG, "sendPlayPause");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_PLAY_PAUSE);
		context.sendBroadcast(intent);
	}
	private void sendStop(Context context){
		//Log.d(TAG, "sendStop");
		Intent intent = new Intent(LOCK_SCREEN_ACTION);
		intent.putExtra(ACTION, ACTION_STOP);
		context.sendBroadcast(intent);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			
			switch (event.getKeyCode()) {
			case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
				sendPlayPause(context);
			break;
			case (KeyEvent.KEYCODE_MEDIA_PLAY): 
				sendPlay(context);
			break;
			case (KeyEvent.KEYCODE_MEDIA_PAUSE): 
				sendPause(context);
			break;
			case (KeyEvent.KEYCODE_MEDIA_NEXT):
				sendNext(context);
			break;
			case (KeyEvent.KEYCODE_MEDIA_PREVIOUS): 
				sendPrev(context);
			break;
			case (KeyEvent.KEYCODE_MEDIA_STOP): 
				sendStop(context);
			break;
			default: break;
			}
		}
	}
}