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
package com.gandulf.guilib.download;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;

public abstract class AbstractDownloader {

	public static final String ACTION_UNZIP_COMPLETE = "com.dsatab.intent.action.ACTION_UNZIP_COMPLETE";

	public static final String INTENT_RESULT = "result";
	public static final String INTENT_EXCEPTION = "exception";
	public static final String INTENT_ERROR = "error";

	public static final int RESULT_OK = 1;
	public static final int RESULT_ERROR = 2;
	public static final int RESULT_CANCELED = 3;

	protected Context context;

	protected String basePath = null;

	protected List<String> todo = new LinkedList<String>();

	/**
	 * 
	 */
	public AbstractDownloader(String basePath, Context context) {
		this.context = context;
		this.basePath = basePath;
	}

	public void addPath(String path) {
		todo.add(path);
	}

	class FileInfo {
		String inFilePath;
		String outFilePath;
	}

	public abstract void downloadZip();

	public abstract void close();
}