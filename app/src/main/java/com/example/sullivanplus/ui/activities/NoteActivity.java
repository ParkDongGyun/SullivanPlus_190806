package com.example.sullivanplus.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.Note;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.Adapter.NoteAdapter;
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
import java.util.List;


public class NoteActivity extends BaseActivity {

	public static int checkCount = 0;

	private View mainLayout;
	FloatingActionButton floatingActionBtn;
	public Toolbar toolbar;
	public Boolean toolbar_active;

	String folderName;
	String fileName;

	public NoteAdapter noteAdapter;
	//ListView listview;
	RecyclerView recyclerView;
	LiveData<List<Note>> noteItemList;
	List<Note> noteItemArrayList;

	//ListView listview_detail;
//	public ArrayList<Boolean> itemcheck;

	/////////////////detail//////////////////////////////////
	LinearLayout notedetail_layout;
	TextView textView_title;
	TextView textView_datetime;
	TextView textView_detail;

	public int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);

		folderName = getFilesDir().getAbsolutePath();
		fileName = "hellohellohello.txt";

		mainLayout = findViewById(R.id.note_mainlayout);
		toolbar_active = false;
		toolbar = findViewById(R.id.note_toolbar);
		toolbar.setTitle(R.string.mode_note);
		setSupportActionBar(toolbar);

		//뒤로가기 버튼 생성
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		recyclerView = findViewById(R.id.note_recyclerview);
		noteItemArrayList = new ArrayList<Note>();

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		notedetail_layout = findViewById(R.id.note_detail_layout);
		textView_title = findViewById(R.id.detail_title);
		textView_detail = findViewById(R.id.detail_detail);
		textView_datetime = findViewById(R.id.detail_datetime);

		Convert_Fragment(false);

		if (notedetail_layout.getVisibility() != View.VISIBLE) {
			getNotes();
		}

		floatingActionBtn = findViewById(R.id.note_add_btn);
		floatingActionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText editText = new EditText(NoteActivity.this);
				AlertDialog.Builder alertDial = EditAlertDialog(getString(R.string.addtitle_alert), getString(R.string.addMessage_alert), getString(R.string.addHint_alert), editText);

				////////////클릭//////////////////
				alertDial.setNegativeButton("저장", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (!editText.getText().toString().equals("")) {
							Long now = System.currentTimeMillis();
							Date date = new Date(now);

							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String title = FindTitle(editText.getText().toString());
							String detail = editText.getText().toString();
							String datetime = simpleDateFormat.format(date);

							SaveNote(title, detail, datetime);
							noteAdapter.notifyDataSetChanged();// DB저장
						} else {
							SullivanResourceData.ShowSnackBar(mainLayout, getString(R.string.saveagain_snackbar));
						}
					}
				});

				alertDial.setPositiveButton("취소", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				alertDial.show();
			}
		});
	}

	private SpannableString GetSpannableString(String name, ForegroundColorSpan foregroundColorSpan) {
		SpannableString spannableString = new SpannableString(name);
		spannableString.setSpan(foregroundColorSpan, 0, spannableString.length(), 0);

		return spannableString;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);

		ForegroundColorSpan foregroundColorSpan_white = new ForegroundColorSpan(Color.WHITE);
		ForegroundColorSpan foregroundColorSpan_gray = new ForegroundColorSpan(Color.GRAY);

		//MenuItem[] menuItems = new MenuItem[3];
		ArrayList<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(menu.findItem(R.id.file_save_btn));
		menuItems.add(menu.findItem(R.id.file_delete_btn));
		menuItems.add(menu.findItem(R.id.file_share_btn));

		String[] toolbarName = getResources().getStringArray(R.array.array_toolbar_name);

		if (notedetail_layout.getVisibility() == View.VISIBLE || toolbar_active) {




			for(int i=0; i<menuItems.size();i++) {
				menuItems.get(i).setTitle(GetSpannableString(toolbarName[i], foregroundColorSpan_white));
			}
			/*SpannableString s = new SpannableString("파일 저장");
			s.setSpan(foregroundColorSpan_white, 0, s.length(), 0);
			savebtn.setTitle(s);

			s = new SpannableString("삭제");
			s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
			deletebtn.setTitle(s);

			s = new SpannableString("공유");
			s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
			sharebtn.setTitle(s);*/
		} else {
			for(int i=0; i<menuItems.size();i++) {
				menuItems.get(i).setTitle(GetSpannableString(toolbarName[i], foregroundColorSpan_gray));
			}
			/*SpannableString s = new SpannableString("파일 저장");
			s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, s.length(), 0);
			savebtn.setTitle(s);
			s = new SpannableString("삭제");
			s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, s.length(), 0);
			deletebtn.setTitle(s);
			s = new SpannableString("공유");
			s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, s.length(), 0);
			sharebtn.setTitle(s);*/
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.file_save_btn:
				if (toolbar_active || notedetail_layout.getVisibility() == View.VISIBLE) {
					if (CheckPermissionStorage()) {
						final EditText editText = new EditText(NoteActivity.this);
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

										if (notedetail_layout.getVisibility() != View.VISIBLE) {
											buf.write(MakeOneString());

										} else {
											buf.write(textView_detail.getText().toString());
										}

										buf.flush();
										buf.close();
										fileOutputStream.close();

										SullivanResourceData.ShowSnackBar(mainLayout, getString(R.string.saveSuccessFront_snackbar) + txt_title + getString(R.string.saveSuccessBack_snackbar));
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									dialog.dismiss();
								} else {
									SullivanResourceData.ShowSnackBar(mainLayout, getString(R.string.saveagain_snackbar));
								}
							}
						});
						alertDial.show();
					}
				}
				return true;

			////삭제 버튼
			case R.id.file_delete_btn:
				ArrayList<Integer> noteID = new ArrayList<>();

				if (notedetail_layout.getVisibility() == View.VISIBLE) {
					noteID.add(id);
					Convert_Fragment(false);
				} else if (toolbar_active) {
					for (int i = 0; i < noteAdapter.sparseBooleanArray.size(); i++) {
						if (noteAdapter.sparseBooleanArray.get(i)) {
							Note noteItem = noteAdapter.getItem(i);
							noteID.add(noteItem.getUid());
						}
					}
				}
				DeleteNote(noteID);
				noteAdapter.sparseBooleanArray.clear();
				noteAdapter.notifyDataSetChanged();

				toolbar.setTitle(R.string.mode_note);

				return true;

			case R.id.file_share_btn:
				if (toolbar_active || notedetail_layout.getVisibility() == View.VISIBLE) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("text/plain");

					if (!(notedetail_layout.getVisibility() == View.VISIBLE)) {
						intent.putExtra(Intent.EXTRA_TEXT, MakeOneString());
					} else {
						intent.putExtra(Intent.EXTRA_TEXT, textView_detail.getText());
					}
					Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
					startActivity(chooser);
				}
				return true;
			case android.R.id.home:
				noteBackButton();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private String FindTitle(String detail) {
		char[] detail_char = detail.toCharArray();
		final char enterKey = '\n';
		int firstEnterkeyPos = detail_char.length;

		for (int i = 0; i < detail_char.length; i++) {
			if (detail_char[i] == enterKey) {
				firstEnterkeyPos = i;
				break;
			}
		}
		String title = "";

		for (int j = 0; j < firstEnterkeyPos; j++) {
			title = title.concat(String.valueOf(detail_char[j]));
		}
		return title;
	}

	@Override
	public void onBackPressed() {
		noteBackButton();
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

	public String MakeOneString() {
		String result = "";

		for (int i = 0; i < noteAdapter.sparseBooleanArray.size(); i++) {
			if (noteAdapter.sparseBooleanArray.get(i)) {
				Note item = noteAdapter.getItem(i);

				if (!TextUtils.isEmpty(result))
					result = item.getDetail();
				else
					result += "\n\n" + item.getDateTime();
			}
		}

		return result;
	}

	public void Convert_Fragment(Boolean isDetailFragment) {
		if (isDetailFragment) {
			notedetail_layout.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
			getNote(id);
			DataSetting_Detail();

			floatingActionBtn.hide();

			toolbar_active = true;

			toolbar.setTitle("노트 상세보기");
			invalidateOptionsMenu();
			recyclerView.setVisibility(View.GONE);
		} else {
			notedetail_layout.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);

			if (floatingActionBtn != null)
				floatingActionBtn.show();

			recyclerView.setVisibility(View.VISIBLE);
			UpdateToolbarText();
		}
	}

	AlertDialog.Builder EditAlertDialog(String title, String message, String editHint, EditText editText) {
		AlertDialog.Builder alertDial = new AlertDialog.Builder(NoteActivity.this);
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

	public void UpdateToolbarText() {
		if (noteAdapter == null)
			return;

		int count = 0;
		for (int i = 0; i < noteItemArrayList.size(); i++) {
			if (noteAdapter.sparseBooleanArray.get(i))
				count++;
		}
		if (count == 0) {
			noteAdapter.sparseBooleanArray.clear();
			toolbar.setTitle(R.string.mode_note);
			toolbar_active = false;
		} else {
			toolbar_active = true;
			toolbar.setTitle(count + "개 선택");
		}

		invalidateOptionsMenu();
	}

	void noteBackButton() {
		if (notedetail_layout.getVisibility() == View.VISIBLE) {
			Convert_Fragment(false);
		} else {
			finish();
		}
	}

	void DataSetting_Detail() {
		Note note = SullivanResourceData.sullivanDatabase.noteDao().getNote(id);

		if (note != null) {
			textView_title.setText(note.getTitle());
			textView_detail.setText(note.getDetail());
			textView_datetime.setText(note.getDateTime());
		}
	}

	private void DeleteNote(ArrayList<Integer> noteNum) {
		for(int i=0; i<noteNum.size();i++)
		{
			Note note = SullivanResourceData.sullivanDatabase.noteDao().getNote(noteNum.get(i));
			SullivanResourceData.sullivanDatabase.noteDao().Delete(note);
			noteItemArrayList.remove(note);
		}

		noteAdapter.setNoteItemArrayList(noteItemArrayList);
	}

	private void SaveNote(String title, String detail, String datetime) {

		Note note = new Note(title, detail, datetime);
		SullivanResourceData.sullivanDatabase.noteDao().insert(note);
		noteItemArrayList.add(SullivanResourceData.sullivanDatabase.noteDao().getNoteLast());

		noteAdapter.setNoteItemArrayList(noteItemArrayList);
	}

	private void getNotes() {
		noteItemArrayList.clear();

		noteItemList = SullivanResourceData.sullivanDatabase.noteDao().getDataAll();
		noteItemArrayList = SullivanResourceData.sullivanDatabase.noteDao().getAll();
		noteAdapter = new NoteAdapter(noteItemArrayList, this);
		recyclerView.setAdapter(noteAdapter);
	}

	private void getNote(int id) {
		Note note = SullivanResourceData.sullivanDatabase.noteDao().getNote(id);
	}
}
