package com.dk.play.adv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dk.play.util.AdvActLibrary2.UploadCancel;

public class AdvReceiver extends BroadcastReceiver{
	private static final String TAG = "AdvRecevier";
	
	@Override
	public void onReceive(Context context, Intent i) {
		Log.d(TAG, "onReceive");
		Log.d(TAG, "Start Service");
		
		//Intent service = new Intent(context, AdvService.class);

		//context.startService(service);
		
		Intent intent = new Intent(UploadCancel.REMOTE_CONTROL);
		intent.putExtra(UploadCancel.ACTION, i.getAction());
		context.sendBroadcast(intent);
	}
}