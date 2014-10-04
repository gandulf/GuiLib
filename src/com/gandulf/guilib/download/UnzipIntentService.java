package com.gandulf.guilib.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.gandulf.guilib.R;
import com.gandulf.guilib.util.Debug;

@TargetApi(value = Build.VERSION_CODES.GINGERBREAD)
public class UnzipIntentService extends IntentService {

	public static final String INTENT_BASEPATH = "basePath";

	public static final String INTENT_DOWNLOAD_ID = "downloadId";

	private NotificationManager notificationManager;
	private Notification notification;

	private DownloadManager downloadManager;

	/**
	 * @param name
	 */
	public UnzipIntentService() {
		super("UnzipIntentService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// the next two lines initialize the Notification, using the
		// configurations above
		notification = new Notification(android.R.drawable.stat_sys_download, "Unpacking package",
				System.currentTimeMillis());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		String errorDescription = null;
		Exception caughtException = null;

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

		String basePath = intent.getStringExtra(INTENT_BASEPATH);
		long downloadId = intent.getLongExtra(INTENT_DOWNLOAD_ID, -1);

		int result = Downloader.RESULT_OK;
		File baseDir = null;
		if (!TextUtils.isEmpty(basePath) && downloadId != -1) {
			// Create a directory in the SDCard to store the files
			baseDir = new File(basePath);
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}

			ZipInputStream inputStream = null;
			try {
				// Open the ZipInputStream
				ParcelFileDescriptor pfd = downloadManager.openDownloadedFile(downloadId);

				inputStream = new ZipInputStream(new ParcelFileDescriptor.AutoCloseInputStream(pfd));

				// Loop through all the files and folders
				for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

					notification.setLatestEventInfo(this, "Unpacking...", entry.getName(), contentIntent);
					notificationManager.notify(DownloadBroadcastReceiver.UNZIP_ID, notification);

					Debug.verbose("Extracting: " + entry.getName() + "...");

					File innerFile = new File(baseDir, entry.getName());
					// if (innerFile.exists()) {
					// innerFile.delete();
					// }

					// Check if it is a folder
					if (entry.isDirectory()) {
						// Its a folder, create that folder
						innerFile.mkdirs();
					} else {
						// Create a file output stream
						BufferedOutputStream bufferedOutputStream = null;
						try {
							if (!innerFile.getParentFile().canWrite()) {
								errorDescription = "DsaTab erhielt keine Schreibrechte für folgende Datei:"
										+ innerFile.getAbsolutePath()
										+ ". Der häufigste Grund hierfür ist, dass die SD-Karte gerade vom PC verwendet wird. Trennen am besten das Kabel zwischen Smartphone und Pc und versuche es erneut.";
								result = Downloader.RESULT_ERROR;
								break;
							}
							FileOutputStream outputStream = new FileOutputStream(innerFile.getAbsolutePath());
							final int BUFFER = 2048;

							// Buffer the output to the file
							bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER);

							// Write the contents
							int count = 0;
							byte[] data = new byte[BUFFER];
							while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
								bufferedOutputStream.write(data, 0, count);
							}

							// Flush and close the buffers
							bufferedOutputStream.flush();
							bufferedOutputStream.close();

						} catch (Exception e) {
							Debug.error(e);
							caughtException = e;
							result = Downloader.RESULT_ERROR;
							break;
						} finally {
							if (bufferedOutputStream != null)
								bufferedOutputStream.close();
						}
					}

					// Close the current entry
					inputStream.closeEntry();
				}
				inputStream.close();

			} catch (Exception e) {
				Debug.error(e);
				caughtException = e;
				result = Downloader.RESULT_ERROR;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}

		} else {
			result = Downloader.RESULT_CANCELED;
		}
		switch (result) {
		case Downloader.RESULT_OK:
			notification.setLatestEventInfo(this, "Unpacking completed", getString(R.string.download_finished),
					contentIntent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.icon = android.R.drawable.stat_sys_download_done;
			notificationManager.notify(DownloadBroadcastReceiver.UNZIP_ID, notification);

			MediaScannerWrapper wrapper = new MediaScannerWrapper(getApplicationContext(), baseDir.getAbsolutePath(),
					"image/*");
			wrapper.scan();

			break;
		case Downloader.RESULT_CANCELED:
			notificationManager.cancel(DownloadBroadcastReceiver.UNZIP_ID);
			break;
		case Downloader.RESULT_ERROR:
			if (errorDescription == null) {
				notification.setLatestEventInfo(this, "Unpacking failed", getString(R.string.download_error),
						contentIntent);
			} else {
				notification.setLatestEventInfo(this, "Unpacking failed", errorDescription, contentIntent);
			}
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.icon = android.R.drawable.stat_sys_warning;
			notificationManager.notify(DownloadBroadcastReceiver.UNZIP_ID, notification);
			break;
		}

		downloadManager.remove(downloadId);

		Intent broadcastIntent = new Intent(Downloader.ACTION_UNZIP_COMPLETE);
		broadcastIntent.putExtra(Downloader.INTENT_RESULT, result);
		broadcastIntent.putExtra(Downloader.INTENT_EXCEPTION, caughtException);
		broadcastIntent.putExtra(Downloader.INTENT_ERROR, errorDescription);
		sendBroadcast(broadcastIntent);

	}
}
