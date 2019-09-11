package com.example.sullivanplus.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.Adapter.ListViewAdapter;

import java.util.ArrayList;

public class CommunityActivity extends BaseActivity {
	//SullivanResourceData sullivanResourceData;
	//ListView listView;
	RecyclerView recyclerView;
	ArrayList<String> stringArrayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);

		recyclerView = findViewById(R.id.community_recyclerview);
		stringArrayList = new ArrayList<>();

		Toolbar toolbar = findViewById(R.id.community_toolbar);
		toolbar.setTitle(R.string.mode_community);
		setSupportActionBar(toolbar);

		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		DataSetting();
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

	private void DataSetting() {
		for (int i = 0; i < getResources().getStringArray(R.array.array_community_items).length; i++)
			stringArrayList.add(getResources().getStringArray(R.array.array_community_items)[i]);

		recyclerView.setAdapter(new ListViewAdapter(stringArrayList));
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


		SullivanResourceData.curr_activity = this;
	}
}
