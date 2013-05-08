package com.gandulf.guilib.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

import com.gandulf.guilib.R;
import com.gandulf.guilib.util.Debug;
import com.gandulf.guilib.util.ResUtil;

/**
 * @author Ganymede
 * 
 */
public class VersionInfoDialog extends AlertDialog {

	public static final String KEY_NEWS_VERSION = "newsversion";

	private WebView webView;

	private String content;

	private int seenVersion = -1;

	private boolean donateVersion;

	private Integer donateContentId;

	private String donateUrl;

	private Class<?> rawClass;

	public static boolean newsShown = false;

	private SharedPreferences preferences;

	/**
	 * @param context
	 */
	public VersionInfoDialog(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public VersionInfoDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public VersionInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public boolean isDonateVersion() {
		return donateVersion;
	}

	public void setDonateVersion(boolean donateVersion) {
		this.donateVersion = donateVersion;
	}

	public Integer getDonateContentId() {
		return donateContentId;
	}

	public void setDonateContentId(Integer donateContentId) {
		this.donateContentId = donateContentId;
	}

	public Class<?> getRawClass() {
		return rawClass;
	}

	public void setRawClass(Class<?> rawClass) {
		this.rawClass = rawClass;
	}

	protected void init() {
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		setTitle(R.string.news_title);

		webView = new WebView(getContext());
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setButton(BUTTON_POSITIVE, getContext().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		setView(webView);
	}

	public boolean hasContent() {
		return !TextUtils.isEmpty(content);
	}

	public int getPackageVersion() {
		int version = -1;
		try {
			PackageInfo manager = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			version = manager.versionCode;
		} catch (NameNotFoundException e) {
			// Handle exception
		}
		return version;
	}

	public void show(boolean showAll) {
		if (showAll) {
			seenVersion = -1;
		} else {
			seenVersion = preferences.getInt(KEY_NEWS_VERSION, -1);
		}
		show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onStart()
	 */
	@Override
	protected void onStart() {
		newsShown = true;
		setSeenVersion(seenVersion);

		super.onStart();
	}

	private void updateView() {
		if (!TextUtils.isEmpty(donateUrl) && isDonateVersion()) {
			Debug.verbose("Setting donate button");
			setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_donate),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							Uri uriUrl = Uri.parse(VersionInfoDialog.this.donateUrl);
							final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
							getContext().startActivity(launchBrowser);
						}
					});
		}
	}

	private void setSeenVersion(Integer seenVersion) {

		StringBuilder summary = new StringBuilder();

		if (donateVersion && donateContentId != null) {

			if (donateContentId > 0) {
				String content = ResUtil.loadResToString(donateContentId, getContext());
				if (content != null)
					summary.append(content);
			}

		}

		int version = getPackageVersion();
		if (version > seenVersion) {

			while (version > seenVersion) {

				int stringId = ResUtil.getString(rawClass, "news_" + version);
				if (stringId > 0) {
					String content = ResUtil.loadResToString(stringId, getContext());
					if (content != null)
						summary.append(content);
				}
				version--;
			}

		}

		Editor editor = preferences.edit();
		editor.putInt(KEY_NEWS_VERSION, version);
		editor.commit();

		if (!TextUtils.isEmpty(summary)) {
			content = summary.toString();
		} else {
			content = null;
		}

		if (webView != null && content != null) {
			webView.loadDataWithBaseURL("/fake", content, "text/html", "utf-8", null);
		}
	}

	public String getDonateUrl() {
		return donateUrl;
	}

	public void setDonateUrl(String donateUrl) {
		this.donateUrl = donateUrl;
		updateView();
	}

}