package com.example.sullivanplus.ui.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;

public class IntroActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);

		SullivanResourceData.SetSullivanDataBase(getApplicationContext());

/*		Log.d("size", "x : "+Float.toString(size.x));
		Log.d("size", "y : "+Float.toString(size.y));*/
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(getApplicationContext(), PermissionActivity.class);
				startActivity(intent);

				finish();
			}
		}, 3000);
	}
}
