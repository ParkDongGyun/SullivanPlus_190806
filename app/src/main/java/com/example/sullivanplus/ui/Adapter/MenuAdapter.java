package com.example.sullivanplus.ui.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.activities.CommunityActivity;
import com.example.sullivanplus.ui.activities.HelpActivity;
import com.example.sullivanplus.ui.activities.MainActivity;
import com.example.sullivanplus.ui.activities.Note2Activity;
import com.example.sullivanplus.ui.activities.SettingActivity;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>{

	private ArrayList<com.example.sullivanplus.ui.Adapter_Item.MenuItem> menuItemArrayList;
	private MainActivity mainActivity;

	public MenuAdapter(ArrayList<com.example.sullivanplus.ui.Adapter_Item.MenuItem> menuItemArrayList, MainActivity mainActivity) {
		this.menuItemArrayList = menuItemArrayList;
		this.mainActivity = mainActivity;
	}
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View view = layoutInflater.inflate(R.layout.activity_menu_item, parent, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

		holder.textView.setText(menuItemArrayList.get(position).getName());
		holder.imageView.setImageDrawable(menuItemArrayList.get(position).getImg());

		if (SullivanResourceData.Curr_Mode.equals(holder.textView.getText())) {
			int color = ContextCompat.getColor(mainActivity, R.color.select_sullivan_color);
			holder.imageView.setColorFilter(color);
			holder.textView.setTextColor(color);

		}

		holder.button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;

				String modeName = menuItemArrayList.get(position).getName();
				if(modeName.equals(mainActivity.getString(R.string.mode_note))) {
//					intent = new Intent(mainActivity, NoteActivity.class);
					intent = new Intent(mainActivity, Note2Activity.class);
				} else if(modeName.equals(mainActivity.getString(R.string.mode_community))) {
					intent = new Intent(mainActivity, CommunityActivity.class);
				} else if(modeName.equals(mainActivity.getString(R.string.mode_help))) {
					intent = new Intent(mainActivity, HelpActivity.class);
				} else if(modeName.equals(mainActivity.getString(R.string.mode_setting))) {
					intent = new Intent(mainActivity, SettingActivity.class);
				} else {
					SullivanResourceData.Curr_Mode = holder.textView.getText().toString();
					mainActivity.ActiveCameraView(SullivanResourceData.Curr_Mode);
				}

				if (mainActivity.result_text.getVisibility() != View.GONE)
					mainActivity.result_text.setVisibility(View.GONE);
				if (intent != null)
					mainActivity.startActivity(intent);
			}
		});
	}

	@Override
	public int getItemCount() {
		return menuItemArrayList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		Button button;
		ImageView imageView;
		TextView textView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			button = itemView.findViewById(R.id.menu_item_btn);
			imageView = itemView.findViewById(R.id.menu_item_image);
			textView = itemView.findViewById(R.id.menu_item_text);
		}
	}
}
/*
public class MenuAdapter extends BaseAdapter {
	private ArrayList<MenuItem> menuitems = new ArrayList<>();

	@Override
	public int getCount() {
		return menuitems.size();
	}

	@Override
	public MenuItem getItem(int position) {
		return menuitems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	//public Context context;
	//public Application application;
	public int layout;
	public LayoutInflater inf;
	public MainActivity mainActivity;
	//public SullivanResourceData sullivanResourceData;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inf.inflate(layout, null);

		Button button = convertView.findViewById(R.id.menu_item_btn);

		//메뉴 아이템 받아오기
		final MenuItem menuItem = getItem(position);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;

				if (menuItem.getPosition() < 7) {
					SullivanResourceData.Curr_Mode = menuItem.getName();
					mainActivity.Init();
				} else {
					switch (menuItem.getPosition()) {
						case 7:
							intent = new Intent(mainActivity, NoteActivity.class);
//							mainActivity.startActivity(intent); // 다음 화면으로 넘어감
							break;
						case 8:
							intent = new Intent(mainActivity, CommunityActivity.class);
//							mainActivity.startActivity(intent); // 다음 화면으로 넘어감
							break;
						case 9:
							intent = new Intent(mainActivity, HelpActivity.class);
//							mainActivity.startActivity(intent); // 다음 화면으로 넘어감
							break;
						case 10:
							intent = new Intent(mainActivity, SettingActivity.class);
//							mainActivity.startActivity(intent); // 다음 화면으로 넘어감
							break;
						default:
//							intent = null;
							mainActivity.result_text.setVisibility(View.GONE);
							break;
					}

					if(intent != null)
						mainActivity.startActivity(intent);
				}
			}
		});

		//메뉴 아이템 이미지
		ImageView iconImg = convertView.findViewById(R.id.menu_item_image);
		iconImg.setImageDrawable(menuItem.getImg());

		//메뉴 아이템 텍스트
		TextView textView = convertView.findViewById(R.id.menu_item_text);
		textView.setText(menuItem.getName());

		if (SullivanResourceData.Curr_Mode.equals(textView.getText())) {
			int color = ContextCompat.getColor(mainActivity, R.color.select_sullivan_color);
			iconImg.setColorFilter(color);
			textView.setTextColor(color);

		}
		return convertView;
	}

	public void addItem(String name, Drawable img, int position) {
		MenuItem menuItem = new MenuItem();

		menuItem.setName(name);
		menuItem.setImg(img);
		menuItem.setPosition(position);

		menuitems.add(menuItem);
	}
}

*/

