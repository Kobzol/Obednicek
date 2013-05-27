package com.ghrabuvka.obednicek;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ServiceAutoStarter extends BroadcastReceiver {
	  @Override
	  public void onReceive(Context context, Intent intent) {
		  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		  if (settings.getBoolean("pref_service", true))
		  {
			  context.startService(new Intent(context, DnesniObed.class));
		  }
	  }
	}