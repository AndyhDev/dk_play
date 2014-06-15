package com.dk.play.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dk.play.service.PlayService.RemoteControl;

public class RemoteControlReceiver extends BroadcastReceiver{
	private static final String TAG = "RemoteControlRecevier";

	public static final String REMOTE_CONTROL_RECECIVER = "REMOTE_CONTROL_RECEIVER";
	public static final String ACTION_START = "action_start";
	
	@Override
	public void onReceive(Context context, Intent i) {
		Log.d(TAG, "onReceive");
		Log.d(TAG, "Start Service");
		
		Intent service = new Intent(context, PlayService.class);
		Bundle extras = i.getExtras();
		if(extras != null){
			if(extras.containsKey(PlayService.WIDGET_SONG_ID)){
				service.setAction(ACTION_START);
				service.putExtra(PlayService.WIDGET_SONG_ID, extras.getLong(PlayService.WIDGET_SONG_ID));
				service.putExtra(RemoteControl.ACTION, i.getAction());
			}
		}
		context.startService(service);
		
		Intent intent = new Intent(RemoteControl.REMOTE_CONTROL);
		intent.putExtra(RemoteControl.ACTION, i.getAction());
		context.sendBroadcast(intent);
	}
}