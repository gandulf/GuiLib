package com.gandulf.guilib.listener;

import android.widget.Checkable;

public interface CheckableListenable extends Checkable {

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener);

    /**
     * Set the state of this component to the given value, without applying the
     * corresponding animation, and without firing an event.
     *
     * @param checked The component state.
     */
    public void setCheckedImmediate(boolean checked);
}
