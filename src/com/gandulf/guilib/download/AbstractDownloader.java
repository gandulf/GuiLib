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

/**
 * @author Ganymede
 * 
 */
public abstract class AbstractDownloader {

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