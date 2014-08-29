package com.gandulf.guilib.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HackeyDrawerLayout extends DrawerLayout {

	public HackeyDrawerLayout(Context context) {
		super(context);
	}

	public HackeyDrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HackeyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
}
