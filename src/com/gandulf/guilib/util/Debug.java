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

import android.util.Log;

import com.gandulf.guilib.BuildConfig;

/**
 * Functions and helpers to aid debugging. DebugMode can be toggled .
 */
public class Debug {

	private static String tag = "GuiLib";

	protected static boolean debugMode = BuildConfig.DEBUG;

	public static String getDebugTag() {
		return tag;
	}

	/**
	 * Sets the tag to be used in LogCat debug messages If unset, it will default to "GuiLib"
	 * 
	 * @param tag
	 *            any valid String for LogCat tags
	 */
	public static void setDebugTag(String tag) {
		Debug.tag = tag;
	}

	/**
	 * Prints a warning to LogCat with information
	 * 
	 * @param source
	 *            The source of the warning, such as function name
	 * @param message
	 *            The message to be passed on
	 */
	public static void warning(String source, String message) {
		if (!debugMode)
			return;
		Log.w(tag, source + " - " + message);
		Exception e = new Exception(source + " - " + message);
		e.printStackTrace();
	}

	/**
	 * Prints a warning to LogCat with information
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void warning(String message, Throwable throwable) {
		if (debugMode)
			Log.w(tag, message, throwable);
	}

	/**
	 * Prints a warning to LogCat with information
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void warning(String message) {
		if (!debugMode)
			return;
		Log.w(tag, message);
	}

	/**
	 * Prints to the verbose stream of LogCat with information
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void print(String message) {
		if (!debugMode)
			return;
		Log.v(tag, message);
	}

	/**
	 * Prints to the error stream of LogCat with information
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void error(String message) {
		Log.e(tag, message);
		Exception e = new Exception(message);
		e.printStackTrace();
	}

	public static void error(String message, Throwable e) {
		Log.e(tag, message);
		e.printStackTrace();
	}

	/**
	 * Prints to the error stream of LogCat with information from the engine
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void warn(Throwable t) {
		Log.w(tag, t.getMessage(), t);
	}

	/**
	 * Prints to the error stream of LogCat with information
	 * 
	 * @param message
	 *            The message to be passed on
	 */
	public static void error(Throwable t) {
		Log.e(tag, t.getMessage(), t);
	}

	/**
	 * Prints to the verbose stream of LogCat, with information
	 * 
	 * 
	 * @param method
	 * @param message
	 */
	public static void verbose(String method, String message) {
		if (!debugMode)
			return;
		Log.v(tag, method + " - " + message);
	}

	public static void heap() {
		Log.w("MEMORY",
				"HeapSize: " + (android.os.Debug.getNativeHeapSize() / (1024)) + "kb Used: "
						+ (android.os.Debug.getNativeHeapAllocatedSize() / (1024)) + "kb Free: "
						+ (android.os.Debug.getNativeHeapFreeSize() / (1024)) + "kb");
	}

	/**
	 * Prints to the verbose stream of LogCat, with information
	 * 
	 * 
	 * @param message
	 */
	public static void verbose(String message) {
		if (!debugMode)
			return;
		Log.v(tag, message);
	}

}
