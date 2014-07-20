package com.dk.play;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;

public class SettingsActivity extends PreferenceActivity {
	private SQLiteDataSource datasource;
	private SQLSongList songList;

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		final ListPreference loopModus = (ListPreference) findPreference("loop_modus");
		final ListPreference startSong = (ListPreference) findPreference("start_song");

		datasource = new SQLiteDataSource(this);
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

	}
}
