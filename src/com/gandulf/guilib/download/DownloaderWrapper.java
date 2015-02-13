/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gandulf.guilib.download;

import java.io.File;

import android.content.Context;

/**
 * @author Ganymede
 * 
 */
public class DownloaderWrapper {

	public static AbstractDownloader getInstance(File baseDir, Context context) {
		return getInstance(baseDir.getAbsolutePath(), context);
	}

	/* calling here forces class initialization */
	public static AbstractDownloader getInstance(String basePath, Context context) {

		try {
			Class.forName("android.app.DownloadManager");
			return new DownloaderGinger(basePath, context);
		} catch (Exception ex) {
			return new Downloader(basePath, context);
		}

	}

}
