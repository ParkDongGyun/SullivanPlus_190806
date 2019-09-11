package com.example.sullivanplus.ui.Adapter;

import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.db.Note;

import java.util.List;

public class NoteRecyclerView extends RecyclerView.Adapter<NoteRecyclerView.ViewHolder> {
	private LiveData<List<Note>> items;
	private OnItemClickListener listener;
	private SparseBooleanArray selectedId;
	private OnSelectChanged selectChanged;

	public NoteRecyclerView(LifecycleOwner lifecycleOwner, LiveData<List<Note>> items, OnItemClickListener listener) {
		this.items = items;
		this.listener = listener;
		this.items.observe(lifecycleOwner, notes -> notifyDataSetChanged());
		selectedId = new SparseBooleanArray();
	}

	public Note getItem(int position) {
		return items == null ? null : (position < items.getValue().size() ? items.getValue().get(position) : null);
	}

	@Override
	public int getItemCount() {
		return items == null ? 0 : (items.getValue() == null ? 0 : items.getValue().size());
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.i_note_row, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Note item = items.getValue().get(position);

		if (item != null) {
			String title = item.getTitle();
			holder.tvTitle.setText(!TextUtils.isEmpty(title) ? title : "Unknown");
			String date = item.getDateTime();
			holder.tvDate.setText(!TextUtils.isEmpty(date) ? date : "Unknown");

			holder.cbCheck.setTag(position);
			holder.cbCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
				Integer pos = (Integer) buttonView.getTag();

				if (isChecked)
					selectedId.put(pos, true);
				else
					selectedId.delete(pos);

				selectChanged.onSelectChanged(this);
			});
		}
	}

	public int getCheckCount() {
		return selectedId == null ? 0 : selectedId.size();
	}

	public SparseBooleanArray getSelectedId() {
		return selectedId;
	}

	public void resetSelected() {
		selectedId = new SparseBooleanArray();
	}

	public void setSelectChanged(OnSelectChanged selectChanged) {
		this.selectChanged = selectChanged;
	}

	public interface OnSelectChanged {
		void onSelectChanged(NoteRecyclerView adapter);
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		RelativeLayout rlItem;
		CheckBox cbCheck;
		TextView tvTitle;
		TextView tvDate;

		public ViewHolder(@NonNull View view) {
			super(view);

			rlItem = view.findViewById(R.id.rlItem);
			cbCheck = view.findViewById(R.id.cbCheck);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvDate = view.findViewById(R.id.tvDate);

			rlItem.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			listener.onClick(v, getAdapterPosition());
		}
	}
}
