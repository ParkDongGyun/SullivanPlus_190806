package com.example.sullivanplus.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;

public class PermissionActivity extends AppCompatActivity {
	public static final int PERMISSION_REQUEST_CODE = 100;
	public static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission);

		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			int firstPermission = ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[0]);
			int secondPermission = ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[1]);

			if (firstPermission == PackageManager.PERMISSION_GRANTED && secondPermission == PackageManager.PERMISSION_GRANTED) {
				StartSullivanIntro();
			} else {
				ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.NoCameraDevice_snackbar, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
			Boolean check_result = true;

			for (int result : grantResults) {
				if (result != PackageManager.PERMISSION_GRANTED) {
					check_result = false;
					break;
				}
			}

			if (check_result) {
				StartSullivanIntro();
			} else {
				Toast.makeText(getApplicationContext(), R.string.PermissionDenyMsg_snackbar, Toast.LENGTH_LONG).show();

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				}, 4000);
			}
		}
	}

	void StartSullivanIntro() {
		SullivanResourceData.originBright = getWindow().getAttributes().screenBrightness;
		SullivanResourceData.sharedPreferences = getSharedPreferences(SullivanResourceData.setting_data, MODE_PRIVATE);
		SullivanResourceData.curBright = SullivanResourceData.sharedPreferences.getBoolean(SullivanResourceData.displaylight, false);

		String[] MenuSelect = getResources().getStringArray(R.array.array_setting_menu);

		for (int i = 0; i < MenuSelect.length; i++) {
			SullivanResourceData.choiceMode.add(MenuSelect[i]);
		}

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPreferences = getSharedPreferences(SullivanResourceData.setting_data, MODE_PRIVATE);
				String modeName = sharedPreferences.getString(SullivanResourceData.mode_set, getResources().getString(R.string.mode_ai));

				Intent intent = new Intent(getApplication(), MainActivity.class);
				intent.putExtra(getResources().getString(R.string.curr_mode), modeName); // 저장된 시작 할 모드 가져오기
				startActivity(intent); // 다음 화면으로 넘어감
				finish();
			}
		}, 1000); // 3초 뒤에 runner객체 실행하도록 함
	}
}
