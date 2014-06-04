package com.dk.play.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dk.play.service.PlayService.RemoteControl;

public class RemoteControlReceiver extends BroadcastReceiver{
	private static final String TAG = "RemoteControlRecevier";

	public static final String REMOTE_CONTROL_RECECIVER = "REMOTE_CONTROL_RECEIVER";

	@Override
	public void onReceive(Context context, Intent i) {
		Log.d(TAG, "onReceive");

		Intent service = new Intent(context, PlayService.class);
		context.startService(service);

		Intent intent = new Intent(RemoteControl.REMOTE_CONTROL);
		intent.putExtra(RemoteControl.ACTION, i.getAction());
		context.sendBroadcast(intent);
	}

}