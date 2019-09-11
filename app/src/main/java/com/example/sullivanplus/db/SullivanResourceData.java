package com.example.sullivanplus.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.sullivanplus.R;
import com.example.sullivanplus.ui.activities.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.Set;

public class SullivanResourceData extends AppCompatActivity {
	public static String Curr_Mode = "AI 모드";
	public static Activity curr_activity;

	public final int[] menu_img = getResources().getIntArray(R.array.array_addition_draw);
	public final String[] menu_name = getResources().getStringArray(R.array.array_addition);

	public static MainActivity mainActivity;
	//public static int GetModeImg(int num)
//	{
//		return menu_img[i];
//	}
	//int menu_img3[] = {R.drawable.menu_ai, R.drawable.menu_text, R.drawable.menu_face,R.drawable.menu_image, R.drawable.menu_color, R.drawable.menu_light,R.drawable.menu_glasses, R.drawable.menu_seenote,R.drawable.menu_community, R.drawable.menu_helper, R.drawable.menu_setting};
	//public static final String menu_name[] = {"AI 모드","문자 인식", "얼굴 인식"," 이미지 묘사", "색상 인식", "빛 밝기", "돋보기", "노트 보기", "커뮤니티", "도움말", "설정"};

	public String[] GetMenuName() {
		return menu_name;
	}

	public int[] GetMenuImg() {
		return menu_img;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////


	AdditionInfo[] additionInfos = new AdditionInfo[8];
	public final String addition_Name[] = getResources().getStringArray(R.array.array_addition);//{"영상통화", "플래시켜기", "사진첩", "노트저장", "공유하기", "자동모드", "단일색상", "반전"};
	public final int addition_img[] = getResources().getIntArray(R.array.array_addition_draw);//{R.drawable.addition_call, R.drawable.addition_flashon, R.drawable.addition_picture, R.drawable.addition_save, R.drawable.addition_share, R.drawable.addition_auto, R.drawable.addtion_color_one, R.drawable.addition_banjeon};
	public static Boolean mFlash = false;
	public static Boolean isColorOne = true;
	public static Boolean isReverse = false;
	public static int num_ai[] = {0, 1, 2, 3, 4};
	public static int num_text[] = {0, 1, 2, 3, 4};
	public static int num_face[] = {0, 1};
	public static int num_image[] = {0, 1, 2, 5};
	public static int num_color[] = {0, 1, 6};
	public static int num_light[] = {0, 1};
	public static int num_glasses[] = {0, 1, 7};

	public void SetAdditionInfo() {
		for (int i = 0; i < additionInfos.length; i++) {
			additionInfos[i].SetAll(addition_Name[i], addition_img[i]);
		}
	}

	ModeInfo[] modeInfos = new ModeInfo[7];

	public void OrganizeModeInfo() {
		int num_ai[] = {0, 1, 2, 3, 4};
		int num_text[] = {0, 1, 2, 3, 4};
		int num_face[] = {0, 1};
		int num_image[] = {0, 1, 2, 5};
		int num_color[] = {0, 1, 6};
		int num_light[] = {0, 1};
		int num_glasses[] = {0, 1, 7};
	}

	void SetModeInfo(int num, int addition[]) {
		modeInfos[num].SetAll(menu_name[num], menu_img[num], addition);
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////


	public String[] GetAdditionName() {
		return addition_Name;
	}

	public int[] GetAdditionImg() {
		return addition_img;
	}

	//public static final String PROVIER_URI = "content://com.example.sullivanplus.db.NoteProvider";


	public static SullivanDatabase sullivanDatabase;

	public static void SetSullivanDataBase(Context context) {
		sullivanDatabase = Room.databaseBuilder(context, SullivanDatabase.class, "SullivanDB")
				.allowMainThreadQueries()
				.build();
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////


	static String community_item_name[] = {"문의하기", "설리번+ 평가하기", "친구초대"};

	public static String[] GetCommunityItemName() {
		return community_item_name;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////


	static String help_item_name[] = {"사용법 안내", "모드 안내", "기능 안내", "이용 권한 안내", "오픈소스 라이센스", "앱 정보"};

	public static String[] GetHelpItemName() {
		return help_item_name;
	}

	//public static SharedPreferences.Editor editor;
	public static SharedPreferences sullvanData;
	public static final String setting_data = "SETTING_DATA";

	public static final String result_voice = "result_voice";
	public static final String mode_set = "mode_set";
	public static final String menu_set = "menu_set";
	public static final String text_recog = "text_recog";
	public static final String face_recog = "face_recog";
	public static final String displaylight = "displayBrighten";
	public static final String message_time = "message_time";

	public static float originBright;
	public static Boolean curBright;
	public static int snackbarTime = 1000;
	public static SharedPreferences sharedPreferences;
	public static Set<String> choiceMode = new HashSet<String>();
	/////////////////////////////////////////////////////////////////////////////////////////////////
	static Snackbar snackbar;

	//static int snackBarBG = getResources().getColor(R.color.select_sullivan_color, getTheme());
	public static void ShowSnackBar(View mainLayout, String detail) {
		snackbar = Snackbar.make(mainLayout, detail, snackbarTime);
		snackbar.setActionTextColor(Color.YELLOW).setAction("확인", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				snackbar.dismiss();
			}
		});
		View snackView = snackbar.getView();
		snackView.setBackgroundColor(Color.GRAY);
		snackbar.show();
	}

	public static void HideSnackBar() {
		if (snackbar != null)
			snackbar.dismiss();
	}

	public static void SetDisplayBright(boolean isBright, Activity activity) {
		if (isBright) {
			WindowManager.LayoutParams params;
			params = activity.getWindow().getAttributes();
			SullivanResourceData.originBright = params.screenBrightness;
			params.screenBrightness = 0;
			activity.getWindow().setAttributes(params);
		} else {
			WindowManager.LayoutParams params;
			params = activity.getWindow().getAttributes();
			params.screenBrightness = SullivanResourceData.originBright;
			activity.getWindow().setAttributes(params);
		}
		curBright = isBright;
	}

	private static final int PERMISSION_REQUEST_CODE = 100;
	public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
	public static final String WRITE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

	public static Boolean CheckPermission(String[] permissions, Context context) {
		for (int i = 0; i < permissions.length; i++) {
			if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED)
				return false;
		}
		return true;
	}

	public static void RequestPermissions(String[] permissions, Activity activity) {
		ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
	}
}

class AdditionInfo {
	String additionName;
	int additionImg;

	public void SetAll(String name, int img) {
		this.additionName = name;
		this.additionImg = img;
	}
}

class ModeInfo {
	String modeName;
	int modeImg;

	int additionNum[];

	void setModeName(String name) {
		this.modeName = name;
	}

	void setModeImg(int img) {
		this.modeImg = img;
	}

	public void SetAll(String modeName, int modeImg, int addition[]) {
		this.modeName = modeName;
		this.modeImg = modeImg;
		this.additionNum = addition;
	}

	String getModeName() {
		return modeName;
	}

	int getModeImg() {
		return modeImg;
	}
}
