package com.gandulf.guilib.listener;

import android.widget.Checkable;

public interface CheckableListenable extends Checkable {

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener);

}
