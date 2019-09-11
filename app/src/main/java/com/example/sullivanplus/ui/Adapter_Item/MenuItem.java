package com.example.sullivanplus.ui.Adapter_Item;

import android.graphics.drawable.Drawable;

public class MenuItem {

	private String name;
	private Drawable img;


	public String getName() {
		return name;
	}

	public Drawable getImg() {
		return img;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setImg(Drawable img) {
		this.img = img;
	}

	public MenuItem(String name, Drawable img) {
		this.name = name;
		this.img = img;
	}
}