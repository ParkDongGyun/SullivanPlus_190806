package com.example.sullivanplus.ui.Adapter_Item;

public class ListViewAdapter_ListView  {
	/* 아이템을 세트로 담기 위한 어레이 */
	/*
	private ArrayList<ListView> mItems = new ArrayList<>();

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public ListView getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = parent.getContext();

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.small_listview, parent, false);
		}

		TextView tv_name = (TextView) convertView.findViewById(R.id.listview_item_name);

		MyItem myItem = getItem(position);

		tv_name.setText(myItem.getName());

		return convertView;
	}

	public void addItem(String name) {
		MyItem mItem = new MyItem();

		mItem.setName(name);

		mItems.add(mItem);
	}
	*/
}
