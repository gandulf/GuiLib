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

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
@TargetApi(value = Build.VERSION_CODES.GINGERBREAD)
public class DownloadBroadcastReceiver extends BroadcastReceiver {

	public static final int UNZIP_ID = 1;

	private String basePath;

	public DownloadBroadcastReceiver() {

	}

	public DownloadBroadcastReceiver(String basePath) {
		this.basePath = basePath;
	}

	private void notify(Context context, String message) {

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(android.R.drawable.stat_sys_download, "Unpacking package",
				System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, "DsaTab Download", message, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.icon = android.R.drawable.stat_sys_warning;
		notificationManager.notify(UNZIP_ID, notification);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

			if (downloadId >= 0 && DownloaderGinger.todoUnzip.contains(downloadId)) {

				DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

				Debug.verbose("Received download completed " + downloadId);

				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(downloadId);
				Cursor cursor = downloadManager.query(query);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
						int status = cursor.getInt(columnIndex);
						int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
						int reason = cursor.getInt(columnReason);

						if (status == DownloadManager.STATUS_SUCCESSFUL) {
							Intent serviceIntent = new Intent(context, UnzipIntentService.class);
							serviceIntent.putExtra(UnzipIntentService.INTENT_BASEPATH, basePath);
							serviceIntent.putExtra(UnzipIntentService.INTENT_DOWNLOAD_ID, downloadId);
							context.startService(serviceIntent);
							DownloaderGinger.todoUnzip.remove(downloadId);
						} else if (status == DownloadManager.STATUS_FAILED) {
							notify(context, "Fehler:\n" + reason);
						} else if (status == DownloadManager.STATUS_PAUSED) {
							notify(context, "Pausiert:\n" + reason);
						} else if (status == DownloadManager.STATUS_PENDING) {
							notify(context, "Pending!");
						} else if (status == DownloadManager.STATUS_RUNNING) {
							notify(context, "Läuft!");
						}
					}

					cursor.close();
				}

			}
		}
	}

}