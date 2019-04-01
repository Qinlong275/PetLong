package com.demo.petlong;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private static final String[] permissionsArray = new String[]{
			Manifest.permission.SYSTEM_ALERT_WINDOW,
			Manifest.permission.GET_TASKS
	};
	//还需申请的权限列表
	private List<String> permissionsList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button startFloatWindow = (Button) findViewById(R.id.start_float_window);

		startFloatWindow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (Build.VERSION.SDK_INT >= 23) {
					if (!Settings.canDrawOverlays(MainActivity.this)) {
						Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivityForResult(intent, 1);
					} else {
						//TODO do something you need
					}
				}

				Intent pet = new Intent(MainActivity.this, AppService.class);
				startService(pet);
				Intent intent = new Intent(MainActivity.this, FloatWindowService.class);
				startService(intent);
				finish();
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case 1:
				for (int i = 0; i < permissions.length; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

					} else {
						Toast.makeText(MainActivity.this, "权限被拒绝： " + permissions[i], Toast.LENGTH_SHORT).show();
					}
				}
				break;
			default:
		}
	}

}
