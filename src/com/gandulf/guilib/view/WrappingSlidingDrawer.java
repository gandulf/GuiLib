/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gandulf.guilib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SlidingDrawer;

public class WrappingSlidingDrawer extends SlidingDrawer {

	public WrappingSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		int orientation = attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
		mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
		mVertical = (orientation == SlidingDrawer.ORIENTATION_VERTICAL);
	}

	public WrappingSlidingDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);

		int orientation = attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
		mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
		mVertical = (orientation == SlidingDrawer.ORIENTATION_VERTICAL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
		}

		final View handle = getHandle();
		final View content = getContent();
		measureChild(handle, widthMeasureSpec, heightMeasureSpec);

		if (mVertical) {
			int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
			content.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightSpecMode));
			heightSpecSize = handle.getMeasuredHeight() + mTopOffset + content.getMeasuredHeight();
			widthSpecSize = content.getMeasuredWidth();
			if (handle.getMeasuredWidth() > widthSpecSize)
				widthSpecSize = handle.getMeasuredWidth();
		} else {
			int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
			getContent().measure(MeasureSpec.makeMeasureSpec(width, widthSpecMode), heightMeasureSpec);
			widthSpecSize = handle.getMeasuredWidth() + mTopOffset + content.getMeasuredWidth();
			heightSpecSize = content.getMeasuredHeight();
			if (handle.getMeasuredHeight() > heightSpecSize)
				heightSpecSize = handle.getMeasuredHeight();
		}

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	private boolean mVertical;
	private int mTopOffset;
}
