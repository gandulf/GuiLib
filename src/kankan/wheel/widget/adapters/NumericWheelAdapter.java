/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget.adapters;

import android.content.Context;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {

	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	private int baseValue;

	private int steps = 1;

	// format
	private String format;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 */
	public NumericWheelAdapter(Context context) {
		this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue) {
		this(context, minValue, maxValue, null);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 * @param format
	 *            the format string
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
		super(context);

		this.minValue = minValue;
		this.maxValue = maxValue;
		this.format = format;
	}

	public int getMinValue() {
		return baseValue + minValue;
	}

	public void setRange(int min, int max) {
		if (minValue != min || maxValue != max) {
			this.minValue = min;
			this.maxValue = max;

			notifyDataInvalidatedEvent();
		}
	}

	public void setStepSize(int stepSize) {
		if (this.steps != stepSize) {
			this.steps = stepSize;

			notifyDataInvalidatedEvent();
		}
	}

	public int getStepSize() {
		return steps;
	}

	public void setMinValue(int min) {
		if (minValue != min) {
			this.minValue = min;
			notifyDataInvalidatedEvent();
		}
	}

	public int getMaxValue() {
		return baseValue + maxValue;
	}

	public void setMaxValue(int max) {
		if (maxValue != max) {
			this.maxValue = max;
			notifyDataInvalidatedEvent();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kankan.wheel.widget.adapters.AbstractWheelTextAdapter#getStateForValue
	 * (int)
	 */
	@Override
	protected int getStateForValue(int index) {
		if (isValidIndex(index)) {
			int value = getItem(index);
			if (value < 0)
				return STATE_NEGATIVE_VALUE;
			else
				return STATE_POSITIVE_VALUE;
		} else
			return STATE_DEFAULT_VALUE;

	}

	@Override
	public CharSequence getItemText(int index) {
		if (isValidIndex(index)) {
			int value = (baseValue + minValue + index) * steps;
			return format != null ? String.format(format, value) : Integer.toString(value);
		}
		return null;
	}

	private boolean isValidIndex(int index) {
		return (index >= 0 && index < getItemsCount());
	}

	public int getItem(int index) {
		return (minValue + index) * steps;
	}

	/**
	 * Return position of given value
	 * 
	 * @param value
	 * @return 0 based index position, -1 if no within valid range
	 */
	public int getPosition(int value) {
		int pos = (value - (minValue + baseValue)) / steps;
		if (pos < 0 && pos >= getItemsCount())
			return -1;
		else
			return pos;
	}

	@Override
	public int getItemsCount() {
		return (maxValue - minValue + 1) / steps;
	}

	public int getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(int baseValue) {
		if (this.baseValue != baseValue) {
			this.baseValue = baseValue;
			notifyDataInvalidatedEvent();
		}
	}

}
