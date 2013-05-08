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

import android.graphics.Point;

/**
 * Utility class to bundle mathematical helper methods
 * 
 */
public class MathUtil {

	public static float getRatio(Integer value, Integer ref) {
		if (value != null && ref != null && ref != 0) {
			return ((float) value) / ref;
		} else {
			return 1.0f;
		}
	}

	public static float coordinatesToAngle(int x, int y) {
		double theta = Math.atan((double) -y / (double) x);
		theta = Math.toDegrees(theta);
		if (x >= 0 && -y >= 0) {
			// theta = theta;
		} else if (x < 0 && -y >= 0) {
			theta = 180 + theta;
		} else if (x < 0 && -y < 0) {
			theta = 180 + theta;
		} else if (x > 0 && -y < 0) {
			theta = 360 + theta;
		}

		return (float) theta;
	}

	public static int coordinatesToRadius(int x, int y) {
		return (int) Math.sqrt(x * x + y * y);
	}

	/**
	 * Convert polar coordinates to cartesian.
	 * 
	 * @param x
	 *            is the x-coordinate of the origin.
	 * @param y
	 *            is the y-coordinate of the origin.
	 * @param degrees
	 *            is the number of degrees to go through.
	 * @param radius
	 *            is the length of the radius.
	 * @param pt
	 *            is the point to put the results in.
	 * @return an (x,y) point.
	 */
	public static Point polarToCartesian(int x, int y, double degree, double radius, Point pt) {

		double radian = Math.toRadians(degree);
		// // 1. Convert the polar coordinates.
		// // Normally, the y calculation would be addition instead of
		// // subtraction, but we are dealing with screen coordinates,
		// // which has its y-coordinate at the top and not the bottom.
		pt.x = (int) (x + radius * Math.cos(radian));
		pt.y = (int) (y - radius * Math.sin(radian));

		// // 2. Straighten up the data a little.
		if (Math.abs(pt.x - radius) <= 1) {
			pt.x = (int) radius;
		}
		if (Math.abs(pt.y - radius) <= 1) {
			pt.y = (int) radius;
		}

		return (pt);
	} // of method
}
