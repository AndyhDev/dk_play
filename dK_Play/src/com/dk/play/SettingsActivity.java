package com.dk.play;

import java.io.File;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;
import com.dk.play.util.Util;
import com.dk.preference.Click;

public class SettingsActivity extends LActivity {
	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onPostCreate(savedInstanceState);
		
		FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

		actionBarImage = new ActionBarImage(this);
	}
	
	public class PrefsFragment extends PreferenceFragment {
		private SQLiteDataSource datasource;
		private SQLSongList songList;
		
		@Override
        public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
	
			final ListPreference loopModus = (ListPreference) findPreference("loop_modus");
			final ListPreference startSong = (ListPreference) findPreference("start_song");
			final ListPreference updateInterval = (ListPreference) findPreference("adv_update_interval");
			
			final Click reGenCover = (Click) findPreference("re_gen_cover");
			reGenCover.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					checkCovers();
					return false;
				}
			});
			
			if(android.os.Build.VERSION.SDK_INT < 21){
				final PreferenceCategory lollipop = (PreferenceCategory) findPreference("lollipop");
				final PreferenceScreen screen = (PreferenceScreen) findPreference("screen");
				screen.removePreference(lollipop);
			}
			datasource = new SQLiteDataSource(getActivity());
			datasource.open();
			songList = datasource.getSQLSongList();
			datasource.close();
	
			CharSequence[] entries = songList.getCharSequenceTitle();
			CharSequence[] values = songList.getCharSequenceId();
			startSong.setEntries(entries);
			if(songList.size() > 0){
				startSong.setDefaultValue(songList.get(0).getTitle());
			}
			startSong.setEntryValues(values);
			CharSequence summary = startSong.getEntry();
			if(summary != null){
				startSong.setSummary(summary);
			}
			startSong.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Set the value as the new value
					startSong.setValue(newValue.toString());
					// Get the entry which corresponds to the current value and set as summary
					preference.setSummary(startSong.getEntry().toString());
					return false;
				}
			});
			
			loopModus.setSummary(loopModus.getEntry().toString());
			loopModus.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Set the value as the new value
					loopModus.setValue(newValue.toString());
					// Get the entry which corresponds to the current value and set as summary
					preference.setSummary(loopModus.getEntry().toString());
					return false;
				}
			});
			
			updateInterval.setSummary(updateInterval.getEntry().toString());
			updateInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Set the value as the new value
					updateInterval.setValue(newValue.toString());
					// Get the entry which corresponds to the current value and set as summary
					preference.setSummary(updateInterval.getEntry().toString());
					return false;
				}
			});
		}
	}
	
	private void checkCovers(){
		final ProgressDialog dlg = new ProgressDialog(this);
		dlg.setTitle(R.string.re_gen_cover_title);
		dlg.setMessage(getString(R.string.please_wait));
		dlg.setCancelable(false);
		dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		final SQLiteDataSource datasource = new SQLiteDataSource(this);
		datasource.open();
		final SQLSongList list = datasource.getSQLSongList();
		dlg.setMax(list.size());
		dlg.show();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i < list.size(); i++){
					final int p = i;
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							dlg.setProgress(p);
						}
					});
					
					SQLSong song = list.get(i);
					File cover =  new File(song.getCoverUri().getPath());
					if(song.hasCover()){
						if(!cover.exists()){
							String path = Util.genCover(song);
							if(path != null){
								song.setCover(new File(path).getName());
								datasource.updateSong(song);
							}
						}
					}
				}
				datasource.close();
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						dlg.dismiss();
					}
				});
				
			}
		});
		t.start();
		
	}
}
