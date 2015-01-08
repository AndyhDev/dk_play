package com.dk.play.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.dk.play.R;
import com.dk.play.service.PlayService.RemoteControl;
import com.dk.play.service.RemoteControlReceiver;

public class WidgetRandom extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];


			Intent playIntent = new Intent(context, RemoteControlReceiver.class);
			playIntent.setAction(RemoteControl.ACTION_PLAY_RANDOM);
			PendingIntent pPlayIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);


			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_random);

			views.setOnClickPendingIntent(R.id.play, pPlayIntent);


			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}