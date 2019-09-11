package com.example.sullivanplus.ui.Adapter;

import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.Note;
import com.example.sullivanplus.ui.activities.NoteActivity;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

	private List<Note> noteItemArrayList;
	private NoteActivity noteActivity;

	public SparseBooleanArray sparseBooleanArray;

	public NoteAdapter() {

	}

	public NoteAdapter(List<Note> noteItemArrayList, NoteActivity noteActivity) {
		this.noteItemArrayList = noteItemArrayList;
		this.noteActivity = noteActivity;
	}

	public void setNoteItemArrayList(List<Note> noteItemArrayList) {
		this.noteItemArrayList = noteItemArrayList;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View view = layoutInflater.inflate(R.layout.checkbox_item, parent, false);

		sparseBooleanArray = new SparseBooleanArray();

		return new ViewHolder(view);
	}

	public Note getItem(int position) {
		return noteItemArrayList == null ? null : (position < noteItemArrayList.size() ? noteItemArrayList.get(position) : null);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

		Note item = noteItemArrayList.get(position);

		if (item != null) {
			String title = item.getTitle();
			holder.textView_title.setText(!TextUtils.isEmpty(title) ? title : "Unknown");
			String date = item.getDateTime();
			holder.textView_datetime.setText(!TextUtils.isEmpty(date) ? date : "Unknown");

			holder.linearLayout.setTag(item.getUid());
			holder.linearLayout.setOnClickListener(v -> {
				Integer id = (Integer) v.getTag();
				noteActivity.id = id;
				noteActivity.Convert_Fragment(true);
			});

			holder.checkBox.setTag(position);
			holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Integer pos = (Integer) buttonView.getTag();

					if (isChecked)
						sparseBooleanArray.put(pos, isChecked);
					else
						sparseBooleanArray.delete(pos);

					noteActivity.UpdateToolbarText();
				}
			});

			if(sparseBooleanArray.size() == 0){
				holder.checkBox.setChecked(false);
			}
		}
	}

	@Override
	public int getItemCount() {
		return noteItemArrayList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		TextView textView_title;
		TextView textView_datetime;
		LinearLayout linearLayout;
		CheckBox checkBox;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			textView_title = itemView.findViewById(R.id.note_item_title);
			textView_datetime = itemView.findViewById(R.id.datetime);
			linearLayout = itemView.findViewById(R.id.note_detail_btn);
			checkBox = itemView.findViewById(R.id.list_item_checkbox);
		}
	}
}

/*public class NoteAdapter extends BaseAdapter
{
	public int layout;
	LayoutInflater inf;
	public NoteActivity noteActivity;

	private ArrayList<NoteItem> noteItems = new ArrayList<>();

	@Override
	public int getCount() {
		return noteItems.size();
	}

	@Override
	public NoteItem getItem(int position) {
		return noteItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	//public void DeleteAllItem(){noteItems.clear();}
	@SuppressLint("ClickableViewAccessibility")

	public void ClearItem() {
		noteItems.clear();
	}
	@Override
	public View getView(int position, View view, ViewGroup viewGroup)
	{
		Context context = viewGroup.getContext();

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.checkbox_item, viewGroup, false);
		}

		TextView textView = view.findViewById(R.id.note_item_title);
		textView.setText(getItem(position).getName());

		TextView textView1 = view.findViewById(R.id.datetime);
		textView1.setText(getItem(position).getDate());


		////////////////////////////체크 박스///////////////////
		CheckBox checkBox = view.findViewById(R.id.list_item_checkbox);
		getItem(position).checkBox = checkBox;
		getItem(position).checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.list_item_checkbox);

				noteActivity.CalculateCheckBox();
				noteActivity.invalidateOptionsMenu();
			}
		});


		////////////////////////////////////////상세보기 활성화////////////////////////////////////////////////////
		getItem(position).linearLayout = view.findViewById(R.id.note_detail_btn);
		getItem(position).linearLayout.setTag(getItem(position).getPosition());
		getItem(position).linearLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				noteActivity.id = (Integer)view.getTag();
				noteActivity.Convert_Fragment(true);
			}
		});

		return view;
	}

	public void addAllItems(List<NoteItem> items) {
		if(noteItems != null)
			noteItems.clear();

		noteItems.addAll(items);
		notifyDataSetChanged();
	}


	public void addItem(String title, String date, int position)
	{
		NoteItem noteItem =  new NoteItem();

		noteItem.setName(title);
		noteItem.setDate(date);
		noteItem.setID(position);
		noteItems.add(noteItem);
	}
}*/
