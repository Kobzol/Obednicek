package com.ghrabuvka.obednicek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        DateTimePreference dialogPrefTime = (DateTimePreference) this.findPreference("pref_time");
        String time = settings.getString("pref_displayTime", "6:00");
        String hour = time.substring(0,time.indexOf(":"));
        String minute = time.substring(time.indexOf(":") + 1, time.length());
        if (hour.length() == 1)
        {
        	hour = "0" + hour;
        }
        if (minute.length() == 1)
        {
        	minute = "0" + minute;
        }
        dialogPrefTime.setSummary(hour + ":" + minute);
        
        CheckBoxPreference checkBoxSluzba = (CheckBoxPreference) this.findPreference("pref_service");
        checkBoxSluzba.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				
				CheckBoxPreference changedBox = (CheckBoxPreference) arg0;
				if (changedBox.isChecked())
				{
					getApplicationContext().stopService(new Intent(getApplicationContext(), DnesniObed.class));
				}
				else 
				{
					getApplicationContext().startService(new Intent(getApplicationContext(), DnesniObed.class));
				}
				
				return true;
			}
        	
        	
        });
    
        
    }
}