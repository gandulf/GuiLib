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

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;

@TargetApi(value = Build.VERSION_CODES.GINGERBREAD)
public class DownloaderGinger extends AbstractDownloader {

	public static List<Long> todoUnzip = new LinkedList<Long>();

	private DownloadManager downloadManager;

	private BroadcastReceiver receiver;

	DownloaderGinger(final String basePath, Context context) {
		super(basePath, context);

		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		receiver = new DownloadBroadcastReceiver(basePath);

		context.getApplicationContext().registerReceiver(receiver,
				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	public void downloadZip() {
		for (String path : todo) {
			Request request = new Request(Uri.parse(path));
			todoUnzip.add(downloadManager.enqueue(request));
		}
	}

	@Override
	public void close() {

	}

}
