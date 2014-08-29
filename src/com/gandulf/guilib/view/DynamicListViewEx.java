package com.gandulf.guilib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.widget.AdapterView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

public class DynamicListViewEx extends DynamicListView {

	public interface OnItemCheckedListener {
		public void onItemChecked(AdapterView<?> list, int position, boolean checked);
	}

	private OnItemCheckedListener onItemCheckedListener;

	public DynamicListViewEx(Context context) {
		super(context);
	}

	public DynamicListViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicListViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Threadsafe clone for backward compatibility as {@link SparseBooleanArray#clone()} is supported first in 4.x APIs
	 * 
	 * @param orginal
	 * @return a clone
	 */
	public static SparseBooleanArray clone(final SparseBooleanArray orginal) {
		if (orginal == null)
			return null;
		final SparseBooleanArray clone = new SparseBooleanArray();

		synchronized (orginal) {
			final int size = orginal.size();
			for (int i = 0; i < size; i++) {
				clone.put(i, orginal.get(i));
			}
		}

		return clone;
	}

	@Override
	public void clearChoices() {
		SparseBooleanArray checkedPositions = getCheckedItemPositions();
		if (checkedPositions != null) {
			checkedPositions = clone(checkedPositions);
		}

		super.clearChoices();
		if (onItemCheckedListener != null && checkedPositions != null) {
			for (int i = checkedPositions.size() - 1; i >= 0; i--) {
				if (checkedPositions.valueAt(i)) {
					onItemCheckedListener.onItemChecked(this, checkedPositions.keyAt(i), false);
				}
			}

		}
	}

	@Override
	public void setItemChecked(int position, boolean value) {
		super.setItemChecked(position, value);
		if (onItemCheckedListener != null) {
			onItemCheckedListener.onItemChecked(this, position, value);
		}
	}

	public void setOnItemCheckedListener(OnItemCheckedListener onItemCheckedListener) {
		this.onItemCheckedListener = onItemCheckedListener;
	}

}
