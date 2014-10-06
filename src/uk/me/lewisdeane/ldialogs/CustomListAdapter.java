package uk.me.lewisdeane.ldialogs;

import uk.me.lewisdeane.ldialogs.BaseDialog.Alignment;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gandulf.guilib.R;

/**
 * Created by Lewis on 17/08/2014.
 */
class CustomListAdapter extends ArrayAdapter<String> {

	private int mRes;
	private String[] mItems;
	private TextView mItemView;

	// Colour of items.
	private int mItemColour;

	// Text size of the items
	private int mItemTextSize;

	// Alignment containing where to align the items
	private Alignment mItemAlignment;

	CustomListAdapter(Context _context, int _res, String[] _items) {
		super(_context, _res, _items);

		this.mRes = _res;
		this.mItems = _items;
	}

	public int getItemColour() {
		return mItemColour;
	}

	public void setItemColour(int mItemColour) {
		this.mItemColour = mItemColour;
	}

	public int getItemTextSize() {
		return mItemTextSize;
	}

	public void setItemTextSize(int mItemTextSize) {
		this.mItemTextSize = mItemTextSize;
	}

	public Alignment getItemAlignment() {
		return mItemAlignment;
	}

	public void setItemAlignment(Alignment mItemAlignment) {
		this.mItemAlignment = mItemAlignment;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;

		// Inflate a view if none present.
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(mRes, null);
		}

		// Reference the text view from layout.
		mItemView = (TextView) v.findViewById(R.id.item_dialog_list_item);

		// Apply properties.
		mItemView.setText(mItems[position]);
		mItemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mItemTextSize);
		mItemView.setGravity(CustomDialog.getGravityFromAlignment(mItemAlignment) | Gravity.CENTER_VERTICAL);

		try {
			mItemView.setTextColor(mItemColour);
		} catch (Exception e) {
		}

		return v;
	}
}
