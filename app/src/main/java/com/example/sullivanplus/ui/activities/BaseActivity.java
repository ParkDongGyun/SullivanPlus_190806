package com.example.sullivanplus.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sullivanplus.db.SullivanResourceData;

public class BaseActivity extends AppCompatActivity {

	MainActivity mainActivity;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		SullivanResourceData.HideSnackBar();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(SullivanResourceData.curr_activity.getLocalClassName().contains("MainActivity"))
			mainActivity =(MainActivity) SullivanResourceData.curr_activity;

		if(SullivanResourceData.curBright != null && SullivanResourceData.curr_activity != null)
			SullivanResourceData.SetDisplayBright(SullivanResourceData.curBright, SullivanResourceData.curr_activity);
	}
}
