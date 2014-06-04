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

public class WidgetBig2Receiver extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent nextIntent = new Intent(context, RemoteControlReceiver.class);
    		nextIntent.setAction(RemoteControl.ACTION_NEXT);
    		PendingIntent pNextIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		Intent pauseIntent = new Intent(context, RemoteControlReceiver.class);
    		pauseIntent.setAction(RemoteControl.ACTION_PAUSE);
    		PendingIntent pPauseIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		Intent prevIntent = new Intent(context, RemoteControlReceiver.class);
    		prevIntent.setAction(RemoteControl.ACTION_PREV);
    		PendingIntent pPrevIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    		Intent playIntent = new Intent(context, RemoteControlReceiver.class);
    		playIntent.setAction(RemoteControl.ACTION_PLAY);
    		PendingIntent pPlayIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		Intent loopIntent = new Intent(context, RemoteControlReceiver.class);
    		loopIntent.setAction(RemoteControl.ACTION_LOOP);
    		PendingIntent pLoopIntent = PendingIntent.getBroadcast(context, 0, loopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    		
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
            views.setOnClickPendingIntent(R.id.prev, pPrevIntent);
            views.setOnClickPendingIntent(R.id.pause, pPauseIntent);
            views.setOnClickPendingIntent(R.id.play, pPlayIntent);
            views.setOnClickPendingIntent(R.id.next, pNextIntent);
            views.setOnClickPendingIntent(R.id.loop, pLoopIntent);
            
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}