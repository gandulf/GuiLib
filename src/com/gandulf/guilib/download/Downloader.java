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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.gandulf.guilib.R;
import com.gandulf.guilib.util.Debug;

public class Downloader extends AbstractDownloader implements DialogInterface.OnCancelListener {

	public static final String ACTION_UNZIP_COMPLETE = "com.dsatab.intent.action.ACTION_UNZIP_COMPLETE";
	public static final String INTENT_RESULT = "result";
	public static final String INTENT_EXCEPTION = "exception";
	public static final String INTENT_ERROR = "error";

	public static final int RESULT_OK = 1;
	public static final int RESULT_ERROR = 2;
	public static final int RESULT_CANCELED = 3;
	private ProgressDialog dialog;

	private DownloadZipTask zipTask;

	private Exception caughtException = null;
	private String errorDescription;

	/**
	 * 
	 */
	Downloader(String basePath, Context context) {
		super(basePath, context);
	}

	private static InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		HttpURLConnection httpConn = (HttpURLConnection) conn;
		httpConn.setAllowUserInteraction(false);
		httpConn.setInstanceFollowRedirects(true);
		httpConn.setRequestMethod("GET");
		httpConn.connect();

		response = httpConn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			in = httpConn.getInputStream();
		}

		return in;
	}

	public void downloadZip() {
		if (context != null) {
			dialog = ProgressDialog.show(context, context.getString(R.string.download),
					context.getString(R.string.download_message));

			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setOnCancelListener(this);
		}
		zipTask = new DownloadZipTask();
		zipTask.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content .DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		if (zipTask != null)
			zipTask.cancel(true);
	}

	public void close() {
		todo.clear();
		if (dialog != null && dialog.getWindow() != null) {
			if (dialog.isShowing())
				dialog.dismiss();
			dialog = null;
		}

		zipTask = null;

	}

	class DownloadZipTask extends AsyncTask<String, String, Integer> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Integer doInBackground(String... params) {

			boolean cancel = false;
			// Create a directory in the SDCard to store the files
			File baseDir = new File(basePath);
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}

			for (String path : todo) {
				Debug.verbose("Downloading " + path);
				ZipInputStream inputStream = null;
				try {
					// Open the ZipInputStream
					inputStream = new ZipInputStream(openHttpConnection(path));

					// Loop through all the files and folders
					for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

						if (isCancelled()) {
							return RESULT_CANCELED;
						}

						publishProgress(entry.getName());

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
									return RESULT_ERROR;
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
								return RESULT_ERROR;
							} finally {
								if (bufferedOutputStream != null)
									bufferedOutputStream.close();
							}
						}

						// Close the current entry
						inputStream.closeEntry();
					}
					inputStream.close();

					if (isCancelled()) {
						return RESULT_CANCELED;
					}

				} catch (Exception e) {
					Debug.error(e);
					caughtException = e;
					return RESULT_ERROR;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
			}

			if (isCancelled() || cancel)
				return RESULT_CANCELED;
			else
				return RESULT_OK;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {

			close();
			switch (result) {
			case RESULT_OK:
				if (context != null) {
					Toast.makeText(context, R.string.download_finished, Toast.LENGTH_SHORT).show();
				}
				break;
			case RESULT_CANCELED:
				if (context != null) {
					Toast.makeText(context, R.string.download_canceled, Toast.LENGTH_SHORT).show();
				}
				break;
			case RESULT_ERROR:
				if (context != null) {
					if (errorDescription == null)
						Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(context, errorDescription, Toast.LENGTH_LONG).show();
				}
				break;
			}
			if (context != null) {
				Intent intent = new Intent(Downloader.ACTION_UNZIP_COMPLETE);
				intent.putExtra(Downloader.INTENT_RESULT, result);
				intent.putExtra(Downloader.INTENT_EXCEPTION, caughtException);
				intent.putExtra(Downloader.INTENT_ERROR, errorDescription);
				context.sendBroadcast(intent);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			if (dialog != null)
				dialog.setMessage(values[0]);
		}

	};

}
