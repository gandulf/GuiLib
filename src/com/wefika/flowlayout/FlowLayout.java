/*
 * Copyright 2013 Blaz Solar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wefika.flowlayout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.gandulf.guilib.R;

/**
 * User: Blaz Solar Date: 5/6/13 Time: 8:17 PM
 */
public class FlowLayout extends ViewGroup {

	private int mGravity = Gravity.START | Gravity.TOP;

	private List<List<View>> lines = new ArrayList<List<View>>();
	private List<Integer> lineHeights = new ArrayList<Integer>();
	private List<Integer> lineMargins = new ArrayList<Integer>();

	public FlowLayout(Context context) {
		super(context);
	}

	public FlowLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyle, 0);

		int index = a.getInt(R.styleable.FlowLayout_android_gravity, -1);
		if (index > 0) {
			setGravity(index);
		}

		a.recycle();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		lines.clear();
		lineHeights.clear();
		lineMargins.clear();

		int width = getWidth();
		int height = getHeight();

		int linesSum = 0;

		int lineWidth = 0;
		int lineHeight = 0;
		List<View> lineViews = new ArrayList<View>();

		float horizontalGravityFactor = 0;
		switch ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK)) {
		case Gravity.LEFT:
		default:
			break;
		case Gravity.CENTER_HORIZONTAL:
			horizontalGravityFactor = .5f;
			break;
		case Gravity.RIGHT:
			horizontalGravityFactor = 1;
			break;
		}

		for (int i = 0; i < getChildCount(); i++) {

			View child = getChildAt(i);

			if (child.getVisibility() == View.GONE) {
				continue;
			}

			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			int childHeight = child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin;

			if (lineWidth + childWidth > width) {
				lineHeights.add(lineHeight);
				lines.add(lineViews);
				lineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor));

				linesSum += lineHeight;

				lineHeight = 0;
				lineWidth = 0;
				lineViews = new ArrayList<View>();
			}

			lineWidth += childWidth;
			lineHeight = Math.max(lineHeight, childHeight);
			lineViews.add(child);
		}

		lineHeights.add(lineHeight);
		lines.add(lineViews);
		lineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor));

		linesSum += lineHeight;

		int verticalGravityMargin = 0;
		switch ((mGravity & Gravity.VERTICAL_GRAVITY_MASK)) {
		case Gravity.TOP:
		default:
			break;
		case Gravity.CENTER_VERTICAL:
			verticalGravityMargin = (height - linesSum) / 2;
			break;
		case Gravity.BOTTOM:
			verticalGravityMargin = height - linesSum;
			break;
		}

		int numLines = lineHeights.size();

		int left;
		int top = 0;

		for (int i = 0; i < numLines; i++) {

			lineHeight = lineHeights.get(i);
			lineViews = lines.get(i);
			left = lineMargins.get(i);

			int children = lineViews.size();

			for (int j = 0; j < children; j++) {

				View child = lineViews.get(j);

				if (child.getVisibility() == View.GONE) {
					continue;
				}

				LayoutParams lp = (LayoutParams) child.getLayoutParams();

				// if height is match_parent we need to remeasure child to line height
				if (lp.height == LayoutParams.MATCH_PARENT) {
					int childWidthMode = MeasureSpec.AT_MOST;
					int childWidthSize = lineWidth;

					if (lp.width == LayoutParams.MATCH_PARENT) {
						childWidthMode = MeasureSpec.EXACTLY;
					} else if (lp.width >= 0) {
						childWidthMode = MeasureSpec.EXACTLY;
						childWidthSize = lp.width;
					}

					child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode), MeasureSpec
							.makeMeasureSpec(lineHeight - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY));
				}

				int childWidth = child.getMeasuredWidth();
				int childHeight = child.getMeasuredHeight();

				int gravityMargin = 0;

				if (Gravity.isVertical(lp.gravity)) {
					switch (lp.gravity) {
					case Gravity.TOP:
					default:
						break;
					case Gravity.CENTER_VERTICAL:
					case Gravity.CENTER:
						gravityMargin = (lineHeight - childHeight - lp.topMargin - lp.bottomMargin) / 2;
						break;
					case Gravity.BOTTOM:
						gravityMargin = lineHeight - childHeight - lp.topMargin - lp.bottomMargin;
						break;
					}
				}

				child.layout(left + lp.leftMargin, top + lp.topMargin + gravityMargin + verticalGravityMargin, left
						+ childWidth + lp.leftMargin, top + childHeight + lp.topMargin + gravityMargin
						+ verticalGravityMargin);

				left += childWidth + lp.leftMargin + lp.rightMargin;

			}

			top += lineHeight;
		}

		lines.clear();
		lineHeights.clear();
		lineMargins.clear();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		int width = 0;
		int height = 0;

		int lineWidth = 0;
		int lineHeight = 0;

		int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {

			View child = getChildAt(i);

			if (child.getVisibility() == View.GONE) {
				continue;
			}

			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int childWidthMode = MeasureSpec.AT_MOST;
			int childWidthSize = sizeWidth;

			int childHeightMode = MeasureSpec.AT_MOST;
			int childHeightSize = sizeHeight;

			if (lp.width == LayoutParams.MATCH_PARENT) {
				childWidthMode = MeasureSpec.EXACTLY;
				childWidthSize -= lp.leftMargin + lp.rightMargin;
			} else if (lp.width >= 0) {
				childWidthMode = MeasureSpec.EXACTLY;
				childWidthSize = lp.width;
			}

			if (lp.height >= 0) {
				childHeightMode = MeasureSpec.EXACTLY;
				childHeightSize = lp.height;
			}

			child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
					MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));

			int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

			if (lineWidth + childWidth > sizeWidth) {

				width = Math.max(width, lineWidth);
				lineWidth = childWidth;

				height += lineHeight;
				lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

			} else {
				lineWidth += childWidth;
				lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
			}

			if (i == childCount - 1) {
				width = Math.max(width, lineWidth);
				height += lineHeight;
			}

		}

		setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
				(modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	public void setGravity(int gravity) {
		if (mGravity != gravity) {
			if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
				gravity |= Gravity.START;
			}

			if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
				gravity |= Gravity.TOP;
			}

			mGravity = gravity;
			requestLayout();
		}
	}

	public class LayoutParams extends MarginLayoutParams {

		public int gravity = -1;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FlowLayout_Layout);

			gravity = a.getInt(R.styleable.FlowLayout_Layout_android_layout_gravity, -1);

			a.recycle();
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

	}

}