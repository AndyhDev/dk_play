package com.dk.play;

import android.os.Bundle;
import android.webkit.WebView;

import com.dk.play.util.LActivity;
import com.dk.util.IO;

public class Gpl_v3 extends LActivity{

	private WebView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gpl_v3);
		
		view = (WebView)findViewById(R.id.webView1);
		String data = IO.getAssetAsString(this, "gpl_v3.html");
		
		view.loadData(data, "text/html", "utf-8");
	}
	
}
