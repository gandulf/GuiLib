/**
 * Copyright (C) 2008 The Android Open Source Project
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gandulf.guilib.R;

public class NumberPicker extends LinearLayout implements OnClickListener, OnFocusChangeListener, OnLongClickListener,
		OnEditorActionListener {

	private static final char[] DIGIT_CHARACTERS = new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	public interface Formatter {
		String toString(int value);
	}

	/*
	 * Use a custom NumberPicker formatting callback to use two-digit minutes
	 * strings like "01". Keeping a static formatter etc. is the most efficient
	 * way to do this; it avoids creating temporary objects on every call to
	 * format().
	 */
	public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = new NumberPicker.Formatter() {
		final StringBuilder mBuilder = new StringBuilder();
		final java.util.Formatter mFmt = new java.util.Formatter(mBuilder);
		final Object[] mArgs = new Object[1];

		public String toString(int value) {
			mArgs[0] = value;
			mBuilder.delete(0, mBuilder.length());
			mFmt.format("%02d", mArgs);
			return mFmt.toString();
		}
	};

	private final Handler mHandler;
	private final Runnable mRunnable = new Runnable() {
		public void run() {
			if (mIncrement) {
				setCurrent(mCurrent + 1);
				mHandler.postDelayed(this, mSpeed);
			} else if (mDecrement) {
				setCurrent(mCurrent - 1);
				mHandler.postDelayed(this, mSpeed);
			}
		}
	};

	private final EditText mText;
	private NumberPickerButton mIncrementButton;
	private NumberPickerButton mDecrementButton;
	private final InputFilter mNumberInputFilter;

	private String[] mDisplayedValues;
	protected int mStart = 0;
	protected int mEnd = 100;
	protected int mSteps = 1;
	protected int mCurrent = mStart;
	protected Integer mDefault;
	protected int mPrevious;
	private OnViewChangedListener<NumberPicker> mListener;
	private Formatter mFormatter;
	private long mSpeed = 300;

	private boolean mIncrement;
	private boolean mDecrement;

	private int negativeColor = Color.BLACK;
	private int positiveColor = Color.BLACK;

	public NumberPicker(Context context) {
		this(context, null);
	}

	public NumberPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.number_picker, this, true);
		mHandler = new Handler();
		InputFilter inputFilter = new NumberPickerInputFilter();
		mNumberInputFilter = new NumberRangeKeyListener();
		if (findViewById(R.id.increment) instanceof NumberPickerButton) {
			mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
			mIncrementButton.setOnClickListener(this);
			mIncrementButton.setOnLongClickListener(this);
			mIncrementButton.setNumberPicker(this);
		}
		if (findViewById(R.id.decrement) instanceof NumberPickerButton) {
			mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
			mDecrementButton.setOnClickListener(this);
			mDecrementButton.setOnLongClickListener(this);
			mDecrementButton.setNumberPicker(this);
		}

		setWeightSum(3.0f);
		mText = (EditText) findViewById(R.id.timepicker_input);
		mText.setOnFocusChangeListener(this);
		mText.setFilters(new InputFilter[] { inputFilter });
		mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		mText.setOnEditorActionListener(this);
		mText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// do nothing;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				validateInput(mText);
			}
		});

		if (getOrientation() == NumberPicker.HORIZONTAL) {
			if (mIncrementButton != null) {
				mIncrementButton.setImageResource(R.drawable.timepicker_land_up_btn);
				removeView(mIncrementButton);
				addView(mIncrementButton);
			}

			mText.setBackgroundResource(R.drawable.timepicker_land_input);

			if (mDecrementButton != null) {
				mDecrementButton.setImageResource(R.drawable.timepicker_land_down_btn);
				removeView(mDecrementButton);
				addView(mDecrementButton, 0);
			}

		} else {
			if (mIncrementButton != null) {
				mIncrementButton.getLayoutParams().width = LayoutParams.FILL_PARENT;
				mIncrementButton.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

			}
			if (mDecrementButton != null) {
				mDecrementButton.getLayoutParams().width = LayoutParams.FILL_PARENT;
				mDecrementButton.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

			}
			mText.getLayoutParams().width = LayoutParams.FILL_PARENT;
			mText.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
		}

		if (!isEnabled()) {
			setEnabled(false);
		}

	}

	public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
		mText.setOnEditorActionListener(listener);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mIncrementButton.setEnabled(enabled);
		mDecrementButton.setEnabled(enabled);
		mText.setEnabled(enabled);
	}

	public void setOnViewChangedListener(OnViewChangedListener<NumberPicker> listener) {
		mListener = listener;
	}

	public void setFormatter(Formatter formatter) {
		mFormatter = formatter;
	}

	public int getNegativeColor() {
		return negativeColor;
	}

	public void setNegativeColor(int negativeColor) {
		this.negativeColor = negativeColor;
	}

	public int getPositiveColor() {
		return positiveColor;
	}

	public void setPositiveColor(int positiveColor) {
		this.positiveColor = positiveColor;
	}

	/**
	 * Set the range of numbers allowed for the number picker. The current value
	 * will be automatically set to the start if its out of range.
	 * 
	 * @param start
	 *            the start of the range (inclusive)
	 * @param end
	 *            the end of the range (inclusive)
	 */
	public void setRange(int start, int end) {
		mStart = start;
		mEnd = end;

		if (mCurrent < mStart)
			mCurrent = mStart;
		else if (mCurrent > mEnd)
			mCurrent = mEnd;

		updateView();
	}

	/**
	 * Set the range of numbers allowed for the number picker. The current value
	 * will be automatically set to the start. Also provide a mapping for values
	 * used to display to the user.
	 * 
	 * @param start
	 *            the start of the range (inclusive)
	 * @param end
	 *            the end of the range (inclusive)
	 * @param displayedValues
	 *            the values displayed to the user.
	 */
	public void setRange(int start, int end, String[] displayedValues) {
		mDisplayedValues = displayedValues;
		mStart = start;
		mEnd = end;
		mCurrent = mStart;
		updateView();
	}

	/**
	 * The speed (in milliseconds) at which the numbers will scroll when the the
	 * +/- buttons are longpressed. Default is 300ms.
	 */
	public void setSpeed(long speed) {
		mSpeed = speed;
	}

	public void onClick(View v) {
		validateInput(mText);
		if (!mText.hasFocus())
			mText.requestFocus();

		// now perform the increment/decrement
		if (R.id.increment == v.getId()) {
			setCurrent(mCurrent + mSteps);
		} else if (R.id.decrement == v.getId()) {
			setCurrent(mCurrent - mSteps);
		}
	}

	private String formatNumber(int value) {
		return (mFormatter != null) ? mFormatter.toString(value) : String.valueOf(value);
	}

	public void setCurrent(int current) {
		if (current < mStart) {
			current = mStart;
		} else if (current > mEnd) {
			current = mEnd;
		}

		if (mCurrent != current) {
			mPrevious = mCurrent;
			mCurrent = current;
			notifyChange();
		}
	}

	protected void notifyChange() {
		updateView();
		if (mListener != null) {
			mListener.onChanged(this, mPrevious, mCurrent);
		}
	}

	public void validate() {
		validateInput(mText);
	}

	protected void updateView() {

		/*
		 * If we don't have displayed values then use the current number else
		 * find the correct value in the displayed values for the current
		 * number.
		 */
		if (mDisplayedValues == null) {
			mText.setText(formatNumber(mCurrent));
		} else {
			mText.setText(mDisplayedValues[mCurrent - mStart]);
		}
		mText.setSelection(mText.getText().length());

		if (mCurrent < 0) {
			mText.setTextColor(negativeColor);
		} else if (mCurrent > 0) {
			mText.setTextColor(positiveColor);
		} else {
			mText.setTextColor(Color.BLACK);
		}
	}

	public void onFocusChange(View v, boolean hasFocus) {

		/*
		 * When focus is lost check that the text field has valid values.
		 */
		if (!hasFocus) {
			validateInput(v);
		}
	}

	private void validateInput(View v) {
		CharSequence str = ((TextView) v).getText();
		if (TextUtils.isEmpty(str)) {

			if (getDefault() != null) {
				setCurrent(getDefault());
			} else {
				// Restore to the old value as we don't allow empty values
				updateView();
			}
		} else {
			// Check the new value and ensure it's in range
			setCurrent(getSelectedValue(str.toString()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.TextView.OnEditorActionListener#onEditorAction(android
	 * .widget.TextView, int, android.view.KeyEvent)
	 */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
			if (v.getSelectionEnd() == 0) {
				v.getEditableText().delete(0, 0);
				return true;
			}
		}
		if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			mText.clearFocus();
		}
		return false;
	}

	public void setTextColor(int color) {
		mText.setTextColor(color);
	}

	public void setTypeface(Typeface typeface) {
		mText.setTypeface(typeface);
	}

	/**
	 * We start the long click here but rely on the {@link NumberPickerButton}
	 * to inform us when the long click has ended.
	 */
	public boolean onLongClick(View v) {

		/*
		 * The text view may still have focus so clear it's focus which will
		 * trigger the on focus changed and any typed values to be pulled.
		 */
		mText.clearFocus();

		if (R.id.increment == v.getId()) {
			mIncrement = true;
			mHandler.post(mRunnable);
		} else if (R.id.decrement == v.getId()) {
			mDecrement = true;
			mHandler.post(mRunnable);
		}
		return true;
	}

	public void cancelIncrement() {
		mIncrement = false;
	}

	public void cancelDecrement() {
		mDecrement = false;
	}

	private class NumberPickerInputFilter implements InputFilter {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			if (mDisplayedValues == null) {
				return mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
			}
			CharSequence filtered = String.valueOf(source.subSequence(start, end));
			String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
					+ dest.subSequence(dend, dest.length());
			String str = String.valueOf(result).toLowerCase();
			for (String val : mDisplayedValues) {
				val = val.toLowerCase();
				if (val.startsWith(str)) {
					return filtered;
				}
			}
			return "";
		}
	}

	private class NumberRangeKeyListener extends NumberKeyListener {

		// XXX This doesn't allow for range limits when controlled by a
		// soft input method!
		public int getInputType() {
			return InputType.TYPE_CLASS_NUMBER;
		}

		@Override
		protected char[] getAcceptedChars() {
			return DIGIT_CHARACTERS;
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
			if (filtered == null) {
				filtered = source.subSequence(start, end);
			}

			String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
					+ dest.subSequence(dend, dest.length());

			if ("".equals(result)) {
				return result;
			}
			int val = getSelectedValue(result);

			/*
			 * Ensure the user can't type in a value greater than the max
			 * allowed. We have to allow less than min as the user might want to
			 * delete some numbers and then type a new number.
			 */
			if (val > mEnd) {
				return "";
			} else {
				return filtered;
			}
		}
	}

	private int getSelectedValue(String str) {
		if (mDisplayedValues == null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				/* Ignore as if it's not a number we don't care */
			}
		} else {
			for (int i = 0; i < mDisplayedValues.length; i++) {

				/* Don't force the user to type in jan when ja will do */
				str = str.toLowerCase();
				if (mDisplayedValues[i].toLowerCase().startsWith(str)) {
					return mStart + i;
				}
			}

			/*
			 * The user might have typed in a number into the month field i.e.
			 * 10 instead of OCT so support that too.
			 */
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				/* Ignore as if it's not a number we don't care */
			}
		}
		return mStart;
	}

	public Integer getDefault() {
		return mDefault;
	}

	public void setDefault(Integer mDefault) {
		this.mDefault = mDefault;
	}

	/**
	 * @return the current value.
	 */
	public int getCurrent() {
		return mCurrent;
	}
}