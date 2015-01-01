package com.dk.play;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;

public class Licenses extends LActivity implements OnClickListener {

	private ImageView showGpl;
	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_licenses);
		
		showGpl = (ImageView)findViewById(R.id.show_gpl);
		showGpl.setOnClickListener(this);
		
		actionBarImage = new ActionBarImage(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.licenses, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.show_gpl){
			Intent i = new Intent(this, Gpl_v3.class);
			startActivity(i);
		}
	}
}
