package com.example.sullivanplus.ui.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;

import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder>{

	private ArrayList<String> stringArrayList;

	public ListViewAdapter(ArrayList<String> stringArrayList) {
		this.stringArrayList = stringArrayList;
	}
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View view = layoutInflater.inflate(R.layout.button_item, parent, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.textView.setText(stringArrayList.get(position));
	}

	@Override
	public int getItemCount() {
		return stringArrayList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		TextView textView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			textView = itemView.findViewById(R.id.list_item_name);
		}
	}
}
/*public class ListViewAdapter extends BaseAdapter
{
	private ArrayList<String> list_items = new ArrayList<>();

	@Override
	public int getCount() {	return list_items.size();	}

	@Override
	public String getItem(int position) {	return list_items.get(position);	}

	@Override
	public long getItemId(int position) {	return 0;	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		Context context = viewGroup.getContext();

		if(view == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.button_item, viewGroup,false);
		}

		TextView textView = view.findViewById(R.id.list_item_name);
		String name = getItem(position);
		textView.setText(name);


		return view;
	}

	public void addItem(String name)
	{
		list_items.add(name);
	}
}*/
