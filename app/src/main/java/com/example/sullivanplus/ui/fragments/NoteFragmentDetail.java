package com.example.sullivanplus.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.ui.activities.NoteActivity;

public class NoteFragmentDetail  extends Fragment
{
	View view;

	public NoteActivity noteActivity;

	public int id;

	TextView textView_datetime;
	TextView textView_title;
	TextView textView_detail;

	String title;
	public String detail;
	String datetime;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		view  = inflater.inflate(R.layout.notefragdetail, container, false);

		textView_title = view.findViewById(R.id.detail_title);
		textView_detail = view.findViewById(R.id.detail_detail);
		textView_datetime = view.findViewById(R.id.detail_datetime);


		return view;
		//return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void ViewSetting()
	{

	}
	@Override
	public void onResume() {
		super.onResume();


	}

	public void DeleteNoteDetail()
	{
		SullivanResourceData.sullivanDatabase.noteDao().Delete(SullivanResourceData.sullivanDatabase.noteDao().loadAllByIds(id));
		/*		String where = "_id =" + Integer.toString(id);
		noteActivity.getContentResolver().delete(Uri.parse(SullivanResourceData.PROVIER_URI), where, null);*/
	}
}
