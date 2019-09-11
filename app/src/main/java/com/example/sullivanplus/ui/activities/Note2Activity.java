package com.example.sullivanplus.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.Note;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.Adapter.NoteRecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Note2Activity extends BaseActivity implements NoteRecyclerView.OnSelectChanged {

	private Toolbar toolbar;
	private FrameLayout frameLayout;
	private FloatingActionButton btnNote;

	private RecyclerView recyclerView;
	private NoteRecyclerView adapter;

	//노트 상세보기
	private TextView detail_title;
	private TextView detail_detail;
	private TextView detail_datatime;

	Boolean toolbar_active;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_note2);

		toolbar_active = false;

		//뷰와 연결
		toolbar = findViewById(R.id.toolbar);
		frameLayout = findViewById(R.id.flContainer);
		btnNote = findViewById(R.id.btnNote);

		detail_title = findViewById(R.id.detail_title);
		detail_detail = findViewById(R.id.detail_detail);
		detail_datatime = findViewById(R.id.detail_datetime);

		//툴바 설정
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setTitle(R.string.mode_note);

		//리사이클뷰 설정
		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		adapter = new NoteRecyclerView(this, SullivanResourceData.sullivanDatabase.noteDao().getDataAll(), (view, position) -> {
			switch (view.getId()) {
				case R.id.rlItem:
					frameLayout.setVisibility(View.VISIBLE);
					Log.i("Test","클릭");
					break;
			}
		});
		adapter.setSelectChanged(this);
		recyclerView.setAdapter(adapter);

		btnNote.setOnClickListener(v -> newNote());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);

		ForegroundColorSpan foregroundColorSpan_white = new ForegroundColorSpan(Color.WHITE);
		ForegroundColorSpan foregroundColorSpan_gray = new ForegroundColorSpan(Color.GRAY);

		ArrayList<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(menu.findItem(R.id.file_save_btn));
		menuItems.add(menu.findItem(R.id.file_delete_btn));
		menuItems.add(menu.findItem(R.id.file_share_btn));

		String[] toolbarName = getResources().getStringArray(R.array.array_toolbar_name);

		if (frameLayout.getVisibility() == View.VISIBLE || toolbar_active) {
			for(int i=0; i<menuItems.size();i++) {
				menuItems.get(i).setTitle(GetSpannableString(toolbarName[i], foregroundColorSpan_white));
			}
		} else {
			for(int i=0; i<menuItems.size();i++) {
				menuItems.get(i).setTitle(GetSpannableString(toolbarName[i], foregroundColorSpan_gray));
			}
		}
		return true;
	}

	Boolean CheckPermissionStorage() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					|| checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
				return false;
			} else
				return true;
		} else
			return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.file_save_btn:
				if (toolbar_active || frameLayout.getVisibility() == View.VISIBLE) {
					if (SullivanResourceData.CheckPermission(new String[]{SullivanResourceData.WRITE_PERMISSION}, this)) {
						SaveNoteText();
					} else {
						SullivanResourceData.RequestPermissions(new String[] {SullivanResourceData.WRITE_PERMISSION}, this);
					}
				}
				break;
			case R.id.file_delete_btn:
				break;
			case R.id.file_share_btn:
				break;
			case android.R.id.home:
				BackButtonEvent();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(SullivanResourceData.CheckPermission(new String[]{SullivanResourceData.WRITE_PERMISSION},this)) {
			SaveNoteText();
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	void SaveNoteText() {
		final EditText editText = new EditText(Note2Activity.this);
		AlertDialog.Builder alertDial = EditAlertDialog(getString(R.string.saveTitle_alert), getString(R.string.saveMessage_alert), getString(R.string.saveHint_alert), editText);

		alertDial.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alertDial.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!editText.getText().toString().equals("")) {
					File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sullivan");

					if (!dir.exists()) {
						dir.mkdirs();
					}

					try {
						String txt_title = editText.getText().toString();
						FileOutputStream fileOutputStream = new FileOutputStream(dir + "/" + txt_title + ".txt", true);

						BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

						if (frameLayout.getVisibility() != View.VISIBLE) {
							buf.write(MakeOneString());

						} else {
							buf.write(detail_detail.getText().toString());
						}

						buf.flush();
						buf.close();
						fileOutputStream.close();

						SullivanResourceData.ShowSnackBar(frameLayout, getString(R.string.saveSuccessFront_snackbar) + txt_title + getString(R.string.saveSuccessBack_snackbar));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					dialog.dismiss();
				} else {
					SullivanResourceData.ShowSnackBar(frameLayout, getString(R.string.saveagain_snackbar));
				}
			}
		});
		alertDial.show();
	}

	public String MakeOneString() {
		String result = "";

		for (int i = 0; i < adapter.getCheckCount(); i++) {
			int noteid = adapter.getSelectedId().keyAt(i);
			Note item = adapter.getItem(noteid);

			if (!TextUtils.isEmpty(result))
				result = item.getDetail();
			else
				result += "\n\n" + item.getDateTime();
		}

		return result;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		BackButtonEvent();
	}

	void BackButtonEvent() {
		if(frameLayout.getVisibility() == View.VISIBLE)
			frameLayout.setVisibility(View.GONE);
		else
			finish();
	}

	@Override
	public void onSelectChanged(NoteRecyclerView adapter) {
		// CheckBox changed.
		setToolbarTitle();
	}

	private void setToolbarTitle() {
		if (adapter != null) {
			int count = adapter.getCheckCount();

			if (count > 0) {
				toolbar.setTitle(count + "개 선택");
				toolbar_active = true;

				return;
			} else
				getSupportActionBar().setTitle(R.string.mode_note);
		} else
			getSupportActionBar().setTitle(R.string.mode_note);

		toolbar_active = false;
	}

	private SpannableString GetSpannableString(String name, ForegroundColorSpan foregroundColorSpan) {
		SpannableString spannableString = new SpannableString(name);
		spannableString.setSpan(foregroundColorSpan, 0, spannableString.length(), 0);

		return spannableString;
	}

	private void newNote() {
		final EditText editText = new EditText(this);
		AlertDialog.Builder alertDial = EditAlertDialog(getString(R.string.addtitle_alert), getString(R.string.addMessage_alert), getString(R.string.addHint_alert), editText);

		alertDial.setNegativeButton("저장", (dialog, which) -> {
			String content = editText.getText().toString();

			if (!TextUtils.isEmpty(content)) {
				Long now = System.currentTimeMillis();
				Date date = new Date(now);

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String title = "";

				if (content.split(System.getProperty("line.separator")).length > 1)
					title = content.split(System.getProperty("line.separator"))[0];
				else
					title = content;

				String datetime = simpleDateFormat.format(date);

				Note note = new Note(title, content, datetime);
				SullivanResourceData.sullivanDatabase.noteDao().insert(note);
			} else {
				SullivanResourceData.ShowSnackBar(findViewById(R.id.root), getString(R.string.saveagain_snackbar));
			}
		});

		alertDial.setPositiveButton("취소", (dialog, which) -> dialog.dismiss());
		alertDial.show();
	}

	AlertDialog.Builder EditAlertDialog(String title, String message, String editHint, EditText editText) {
		AlertDialog.Builder alertDial = new AlertDialog.Builder(this);
		alertDial.setTitle(title);
		alertDial.setMessage(message);

		editText.setHint(editHint);

		FrameLayout container = new FrameLayout(this);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = getResources().getDimensionPixelSize(R.dimen.text_margin);
		params.rightMargin = getResources().getDimensionPixelSize(R.dimen.text_margin);
		editText.setLayoutParams(params);
		container.addView(editText);

		alertDial.setView(container);

		return alertDial;
	}
}
