package com.dk.play.util;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.service.PlayService;

public class OverlayPlayer implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "OverlayPlayer";
	private Context context;
	private PlayService service;
	private WindowManager wm;
	private RelativeLayout layout;
	private WindowManager.LayoutParams wlp;
	private boolean shown = false;
	private SQLSong curSong;
	private Integer loopState;
	private Integer state;
	
	public static final int BOTTOM_POS = 1;
	public static final int TOP_POS = 2;
	private int pos = BOTTOM_POS;


	private ImageButton playBnt;
	private ImageButton nextBnt;
	private ImageButton prevBnt;
	private ImageButton loopBnt;
	private ImageButton moveBnt;
	private ImageButton closeBnt;
	private TextView title;
	private ImageView background;

	public OverlayPlayer(Context context, PlayService service){
		this.context = context;
		this.service = service;

		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		layout = (RelativeLayout) RelativeLayout.inflate(context, R.layout.overlay_player, null);

		wlp = new WindowManager.LayoutParams();
		wlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
		wlp.height = getPx(70);//WindowManager.LayoutParams.WRAP_CONTENT;
		wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		wlp.x = 0;
		wlp.y = 0;
		wlp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wlp.format = PixelFormat.RGBA_8888;

		playBnt = (ImageButton)layout.findViewById(R.id.overlay_player_play);
		playBnt.setOnClickListener(this);

		nextBnt = (ImageButton)layout.findViewById(R.id.overlay_player_next);
		nextBnt.setOnClickListener(this);

		prevBnt = (ImageButton)layout.findViewById(R.id.overlay_player_prev);
		prevBnt.setOnClickListener(this);

		loopBnt = (ImageButton)layout.findViewById(R.id.overlay_player_loop);
		loopBnt.setOnClickListener(this);

		moveBnt = (ImageButton)layout.findViewById(R.id.overlay_player_move);
		moveBnt.setOnClickListener(this);

		closeBnt = (ImageButton)layout.findViewById(R.id.overlay_player_close);
		closeBnt.setOnClickListener(this);

		title = (TextView)layout.findViewById(R.id.overlay_player_title);
		background = (ImageView)layout.findViewById(R.id.overlay_player_bg);
	}

	public int getPx(int dp){
		Resources r = context.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	public void setState(int state){
		this.state = state;
		if(state == PlayService.PLAY_STATE_PLAY){
			playBnt.setImageResource(R.drawable.ic_action_pause);
		}else if(state == PlayService.PLAY_STATE_PAUSE){
			playBnt.setImageResource(R.drawable.ic_action_play);
		}else if(state == PlayService.PLAY_STATE_STOP){
			playBnt.setImageResource(R.drawable.ic_action_play);
		}
	}
	public void setLoopState(int loopState){
		this.loopState = loopState;

		if(loopState == PlayService.LOOP_STATE_NO_LOOP){
			loopBnt.setImageResource(R.drawable.loop);
		}else if(loopState == PlayService.LOOP_STATE_LOOP){
			loopBnt.setImageResource(R.drawable.loop1);
		}else if(loopState == PlayService.LOOP_STATE_LOOP_SONG){
			loopBnt.setImageResource(R.drawable.loop2);
		}else if(loopState == PlayService.LOOP_STATE_SHUFFLE){
			loopBnt.setImageResource(R.drawable.ic_action_shuffle);
		}
	}
	public void setSong(SQLSong song){
		curSong = song;
		String path = Paths.getCoverPath(curSong.getCover());
		File cover = new File(path);
		if(cover.exists()){
			Bitmap bmp = BitmapFactory.decodeFile(path);
			background.setImageBitmap(bmp);
		}else{
			background.setImageDrawable(context.getResources().getDrawable(R.drawable.default_cover));
		}
		title.setText(curSong.getTitle());
	}
	public boolean isShowing(){
		return shown;
	}
	private void setupUi(){
		if(curSong != null){
			String path = Paths.getCoverPath(curSong.getCover());
			File cover = new File(path);
			if(cover.exists()){
				Bitmap bmp = BitmapFactory.decodeFile(path);
				background.setImageBitmap(bmp);
			}else{
				background.setImageDrawable(context.getResources().getDrawable(R.drawable.default_cover));
			}
			title.setText(curSong.getTitle());
		}
		if(loopState != null){
			if(loopState == PlayService.LOOP_STATE_NO_LOOP){
				loopBnt.setImageResource(R.drawable.loop);
			}else if(loopState == PlayService.LOOP_STATE_LOOP){
				loopBnt.setImageResource(R.drawable.loop1);
			}else if(loopState == PlayService.LOOP_STATE_LOOP_SONG){
				loopBnt.setImageResource(R.drawable.loop2);
			}else if(loopState == PlayService.LOOP_STATE_SHUFFLE){
				loopBnt.setImageResource(R.drawable.ic_action_shuffle);
			}
		}
		if(state != null){
			if(state == PlayService.PLAY_STATE_PLAY){
				playBnt.setImageResource(R.drawable.ic_action_pause);
			}else if(state == PlayService.PLAY_STATE_PAUSE){
				playBnt.setImageResource(R.drawable.ic_action_play);
			}else if(state == PlayService.PLAY_STATE_STOP){
				playBnt.setImageResource(R.drawable.ic_action_play);
			}
		}
	}
	public void show(){
		if(!shown){
			curSong = service.getCurSong();
			loopState = service.getLoopState();
			state = service.getPlayState();
			
			wm.addView(layout, wlp);
			shown = true;
			setupUi();
		}
	}
	public void hide(){
		if(shown){
			wm.removeView(layout);
			shown = false;
		}
	}
	public int getPos(){
		return pos;
	}
	public int getStatusBarHeight() { 
	      int result = 0;
	      int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = context.getResources().getDimensionPixelSize(resourceId);
	      } 
	      return result;
	} 
	
	public void move(){
		if(shown){
			if(pos == BOTTOM_POS){
				wlp.gravity = Gravity.TOP | Gravity.LEFT;
				wlp.x = 0;
				wlp.y = getStatusBarHeight();
	            wm.updateViewLayout(layout, wlp);
				pos = TOP_POS;
				moveBnt.setImageResource(R.drawable.ic_action_download);
			}else{
				wlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
				wlp.x = 0;
				wlp.y = 0;
	            wm.updateViewLayout(layout, wlp);
				pos = BOTTOM_POS;
				moveBnt.setImageResource(R.drawable.ic_action_upload);
			}
		}
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.overlay_player_play){
			if(service.isPlaying()){
				service.pause();
			}else{
				service.play();
			}
		}else if(id == R.id.overlay_player_next){
			service.next();
		}else if(id == R.id.overlay_player_prev){
			service.prev();
		}else if(id == R.id.overlay_player_loop){
			service.loop();
		}else if(id == R.id.overlay_player_move){
			move();
		}else if(id == R.id.overlay_player_close){
			hide();
		}
	}
}
