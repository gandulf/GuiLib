package com.gandulf.guilib.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.rokoder.android.lib.support.v4.widget.GridViewCompat;

public class ListViewCompat {

	private ListViewCompat() {

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static SparseBooleanArray getCheckedItemPositions(AdapterView<?> parent) {

		SparseBooleanArray checked = null;
		if (parent instanceof GridViewCompat)
			checked = ((GridViewCompat) parent).getCheckedItemPositions();
		else if (parent instanceof ListView) {
			checked = ((ListView) parent).getCheckedItemPositions();
		} else if (parent instanceof GridView) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				checked = ((GridView) parent).getCheckedItemPositions();
			} else {
				Debug.warning("Using GridView with checked items does not work before honeycomb use gridviewcompat!");
			}
		}

		return checked;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static boolean isItemChecked(AdapterView<?> parent, int position) {
		if (parent instanceof GridViewCompat)
			return ((GridViewCompat) parent).isItemChecked(position);
		else if (parent instanceof ListView) {
			return ((ListView) parent).isItemChecked(position);
		} else if (parent instanceof GridView) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				return ((GridView) parent).isItemChecked(position);
			} else {
				Debug.warning("Using GridView with checked items does not work before honeycomb use gridviewcompat!");

			}
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void setItemChecked(AdapterView<?> parent, int position, boolean checked) {
		if (parent instanceof GridViewCompat)
			((GridViewCompat) parent).setItemChecked(position, checked);
		else if (parent instanceof ListView) {
			((ListView) parent).setItemChecked(position, checked);
		} else if (parent instanceof GridView) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				((GridView) parent).setItemChecked(position, checked);
			} else {
				Debug.warning("Using GridView with checked items does not work before honeycomb use gridviewcompat!");
			}
		}
	}

}
