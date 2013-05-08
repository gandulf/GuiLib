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
package com.gandulf.guilib.util;

import android.graphics.Color;

public class ColorUtil {

	public static int darker(int color) {
		return darker(color, 0.2f);
	}

	public static int darker(int color, float factor) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= (1f - factor); // value component
		color = Color.HSVToColor(hsv);
		return color;
	}

	public static int brighter(int color, float factor) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= (1f + factor); // value component
		for (int i = 1; i < hsv.length; i++) {
			hsv[i] = Math.min(1.0f, hsv[i]);
		}
		color = Color.HSVToColor(hsv);
		return color;
	}

	public static int brighter(int color) {
		return brighter(color, 0.2f);
	}

	/**
	 * Calculates the luminosity of a color
	 * 
	 * @param color
	 * @return int luminosity [0..255]
	 */
	public static int luminosity(int color) {
		return (int) (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color));
	}

	public static int addAlpha(int alpha, int color) {
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}

	/**
	 * @param color
	 * @return
	 */
	public static int getTextColor(int color) {
		if (luminosity(color) > 122)
			return Color.BLACK;
		else
			return Color.WHITE;

	}
}
