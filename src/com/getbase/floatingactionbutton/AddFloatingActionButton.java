package com.getbase.floatingactionbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;

import com.gandulf.guilib.R;

public class AddFloatingActionButton extends FloatingActionButton {
	int mPlusColor;

	public AddFloatingActionButton(Context context) {
		this(context, null);
	}

	public AddFloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AddFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	void init(Context context, AttributeSet attributeSet) {
		if (attributeSet != null) {
			TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.AddFloatingActionButton, 0, 0);
			if (attr != null) {
				try {
					mPlusColor = attr.getColor(R.styleable.AddFloatingActionButton_plusIconColor,
							getColor(android.R.color.white));
				} finally {
					attr.recycle();
				}
			}
		} else {
			mPlusColor = getColor(android.R.color.white);
		}

		super.init(context, attributeSet);
	}

	public int getPlusColor() {
		return mPlusColor;
	}

	public void setPlusColor(int mPlusColor) {
		this.mPlusColor = mPlusColor;
	}

	@Override
	Drawable getIconDrawable() {
		final float iconSize = getDimension(R.dimen.fab_icon_size);
		final float iconHalfSize = iconSize / 2f;

		final float plusSize = getDimension(R.dimen.fab_plus_icon_size);
		final float plusHalfStroke = getDimension(R.dimen.fab_plus_icon_stroke) / 2f;
		final float plusOffset = (iconSize - plusSize) / 2f;

		final Shape shape = new Shape() {
			@Override
			public void draw(Canvas canvas, Paint paint) {
				canvas.drawRect(plusOffset, iconHalfSize - plusHalfStroke, iconSize - plusOffset, iconHalfSize
						+ plusHalfStroke, paint);
				canvas.drawRect(iconHalfSize - plusHalfStroke, plusOffset, iconHalfSize + plusHalfStroke, iconSize
						- plusOffset, paint);
			}
		};

		ShapeDrawable drawable = new ShapeDrawable(shape);

		final Paint paint = drawable.getPaint();
		paint.setColor(mPlusColor);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);

		return drawable;
	}
}
