package com.thebnich.floatinghintedittext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.gandulf.guilib.R;

public class FloatingHintTextView extends TextView {

	private final Paint mFloatingHintPaint = new Paint();
	private final ColorStateList mHintColors;
	private final float mHintScale;

	private CharSequence mHint;

	public FloatingHintTextView(Context context) {
		this(context, null);
	}

	public FloatingHintTextView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.floatingHintTextViewStyle);
	}

	public FloatingHintTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedValue typedValue = new TypedValue();
		getResources().getValue(R.dimen.floatinghintedittext_hint_scale, typedValue, true);
		mHintScale = typedValue.getFloat();

		mHintColors = getHintTextColors();
	}

	@Override
	public int getCompoundPaddingTop() {
		final FontMetricsInt metrics = getPaint().getFontMetricsInt();
		final int floatingHintHeight = (int) ((metrics.bottom - metrics.top) * mHintScale);
		return super.getCompoundPaddingTop() + floatingHintHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (getHint() != null) {
			mHint = getHint();
			setHint(null);
		}
		super.onDraw(canvas);

		if (TextUtils.isEmpty(mHint)) {
			return;
		}

		mFloatingHintPaint.set(getPaint());
		mFloatingHintPaint.setColor(mHintColors.getColorForState(getDrawableState(), mHintColors.getDefaultColor()));

		final float hintPosX = getCompoundPaddingLeft() + getScrollX();
		final float normalHintPosY = getBaseline();
		final float floatingHintPosY = normalHintPosY + getPaint().getFontMetricsInt().top + getScrollY();
		final float normalHintSize = getTextSize();
		final float floatingHintSize = normalHintSize * mHintScale;

		// If we're not animating, we're showing the floating hint, so draw it and bail.
		mFloatingHintPaint.setTextSize(floatingHintSize);
		canvas.drawText(mHint.toString(), hintPosX, floatingHintPosY, mFloatingHintPaint);

	}

}
