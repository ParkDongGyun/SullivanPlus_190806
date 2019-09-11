package com.example.sullivanplus.ui.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.activities.MainActivity;

import java.util.ArrayList;

public class AdditionAdapter extends RecyclerView.Adapter<AdditionAdapter.ViewHolder>{

	private ArrayList<com.example.sullivanplus.ui.Adapter_Item.MenuItem> menuItemArrayList;
	private MainActivity mainActivity;

	public AdditionAdapter(ArrayList<com.example.sullivanplus.ui.Adapter_Item.MenuItem> menuItemArrayList, MainActivity mainActivity) {
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

		holder.button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.result_text.setVisibility(View.INVISIBLE);
				switch (menuItemArrayList.get(position).getName()) {
					case "영상통화":
						break;
					case "플래시 켜기":
						SullivanResourceData.mFlash = true;
						mainActivity.flashlight();

						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_flash_on_snackbar));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						break;
					case "플래시 끄기":
						SullivanResourceData.mFlash = false;
						mainActivity.flashlight();
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_flash_off_snackbar));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						break;
					case "단일색상":
						SullivanResourceData.isColorOne = true;
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_color_one) + mainActivity.getString(R.string.addition_set));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						mainActivity.AdditionDataSetting(mainActivity.SelectAddition(SullivanResourceData.Curr_Mode));
						break;
					case "전체색상":
						SullivanResourceData.isColorOne = false;
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_color_multi) + mainActivity.getString(R.string.addition_set));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						mainActivity.AdditionDataSetting(mainActivity.SelectAddition(SullivanResourceData.Curr_Mode));
						break;
				}
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

/*public class AdditionAdapter extends BaseAdapter {
	public ArrayList<MenuItem> menuitems = new ArrayList<>();

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

	public Application application;
	public Context context;
	public int layout;
	public LayoutInflater inf;
	//public SullivanResourceData sullivanResourceData;
	public MainActivity mainActivity;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final int menu_Num = position;

		if (convertView == null)
			convertView = inf.inflate(layout, null);

		//메뉴 아이템 받아오기
		final MenuItem menuItem = getItem(position);


		//메뉴 아이템 버튼
		final Button button = convertView.findViewById(R.id.menu_item_btn);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(application, menuItem.getName(), Toast.LENGTH_LONG).show();

//				mainActivity.result_text.setVisibility(View.INVISIBLE);
				switch (menuItem.getName()) {
					case "영상통화":
						break;
					case "플래시 켜기":
						SullivanResourceData.mFlash = true;
						mainActivity.flashlight();

						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_flash_on_snackbar));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						break;
					case "플래시 끄기":
						SullivanResourceData.mFlash = false;
						mainActivity.flashlight();
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, mainActivity.getString(R.string.addition_flash_off_snackbar));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						break;
					case "단일색상":
						SullivanResourceData.isColorOne = true;
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, context.getString(R.string.addition_color_one) + context.getString(R.string.addition_set));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						mainActivity.AdditionDataSetting(mainActivity.SelectAddition(SullivanResourceData.Curr_Mode));
						break;
					case "전체색상":
						SullivanResourceData.isColorOne = false;
						SullivanResourceData.ShowSnackBar(mainActivity.relativeLayout, context.getString(R.string.addition_color_multi) + context.getString(R.string.addition_set));
						mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
						mainActivity.AdditionDataSetting(mainActivity.SelectAddition(SullivanResourceData.Curr_Mode));
						break;
				}
				//mainActivity.ActiveCameraView(mainActivity.curr_mode_txt.getText().toString());
			}});

		//메뉴 아이템 이미지
		ImageView imgview = convertView.findViewById(R.id.menu_item_image);
		imgview.setImageDrawable(menuItem.getImg());

		//메뉴 아이템 텍스트
		TextView textView = convertView.findViewById(R.id.menu_item_text);
		textView.setText(menuItem.getName());

		return convertView;
	}

	public void addItem(String name, Drawable img, int position) {
		MenuItem menuItem = new MenuItem(name, img);

		menuItem.setName(name);
		menuItem.setImg(img);
		menuItem.setPosition(position);

		menuitems.add(menuItem);
	}
}*/
