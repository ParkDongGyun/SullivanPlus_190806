package com.example.sullivanplus.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;

public class SettingActivity extends BaseActivity
{
	SullivanResourceData sullivanResourceData;
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		toolbar = findViewById(R.id.setting_toolbar);
		toolbar.setTitle(R.string.mode_setting);
		setSupportActionBar(toolbar);

		//뒤로가기 버튼 생성
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		SullivanResourceData.curr_activity = this;
	}

	@Override
	protected void onStart() {
		super.onStart();

		SullivanResourceData.curr_activity = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}