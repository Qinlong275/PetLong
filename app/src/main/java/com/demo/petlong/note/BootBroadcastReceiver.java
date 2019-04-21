package com.demo.petlong.note;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.demo.petlong.FloatWindowService;

public class BootBroadcastReceiver extends BroadcastReceiver{

	private static final String TAG = "BootBroadcastReceiver";
	private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(ACTION_BOOT)){
			Toast.makeText(context,"开机启动",Toast.LENGTH_SHORT).show();
			Log.i("qinlong","开机启动");
			context.startService(new Intent(context, FloatWindowService.class));
		}

	}
}
