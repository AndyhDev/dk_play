package com.dk.play.util;

import com.dk.play.R;
import com.dk.play.WhatsNew;
import com.dk.util.UiHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class NewPopUp implements OnClickListener {
	private Context context;
	private WindowManager wm;
	private RelativeLayout layout;
	private LayoutParams wlp;
	private ImageButton close;
	
	public NewPopUp(Context context){
		this.context = context;
		
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		layout = (RelativeLayout) RelativeLayout.inflate(context, R.layout.new_popup, null);
		layout.setOnClickListener(this);
		close = (ImageButton) layout.findViewById(R.id.close);
		close.setOnClickListener(this);
		
		wlp = new WindowManager.LayoutParams();
		wlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
		wlp.height = UiHelper.getPx(context, 90);
		wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		wlp.x = 0;
		wlp.y = 0;
		wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wlp.format = PixelFormat.RGBA_8888;
	}
	
	public void show(){
		wm.addView(layout, wlp);
	}
	
	public void hide(){
		wm.removeView(layout);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.close){
			hide();
		}else{
			Intent intent = new Intent(context, WhatsNew.class);
			context.startActivity(intent);
			hide();
		}
	}
}
