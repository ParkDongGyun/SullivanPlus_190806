package com.example.sullivanplus.db;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Note implements Serializable {
	@PrimaryKey(autoGenerate = true)
	private int uid;
	@ColumnInfo(name = "title")
	private String title;
	@ColumnInfo(name = "detail")
	private String detail;
	@ColumnInfo(name = "datetime")
	private String dateTime;

	public Note(String title, String detail, String dateTime) {
		this.uid = uid;
		this.title = title;
		this.detail = detail;
		this.dateTime = dateTime;
	}

	public int getUid() {
		return uid;
	}

	public String getTitle() {
		return title;
	}

	public String getDetail() {
		return detail;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if(this.uid == ((Note) obj).uid)
			return true;
		else
			return false;
		//return super.equals(obj);
	}

	@Override
	public String toString() {
		return "Note{" +
				"uid=" + uid +
				", title='" + title + '\'' +
				", detail='" + detail + '\'' +
				", dateTime='" + dateTime + '\'' +
				'}';
	}
}