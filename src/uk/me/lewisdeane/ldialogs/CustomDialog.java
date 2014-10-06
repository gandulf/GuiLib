package uk.me.lewisdeane.ldialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gandulf.guilib.R;

// Class extends BaseDialog which contains variables and properties applicable to all adaptations of our custom dialogs.
public class CustomDialog extends BaseDialog {

	// Context for dialog to use.
	private Context mContext;

	// The layout view inflated from resources.
	private View mRootView;

	// View holding any custom views passed in.
	private View mCustomView;

	// Colour of items.
	static int mItemColour;

	// Text size of the items
	static int mItemTextSize;

	// Alignment containing where to align the items
	static Alignment mItemAlignment;

	// List Adapter used to populate the list view.
	private ListAdapter mCustomListAdapter;

	// Listener that listens for item click events and then dismisses dialog.
	private DialogInterface.OnClickListener mlistCallbacks;

	private ListView listView;

	private DialogInterface.OnClickListener mPositiveCallback, mNegativeCallback;

	// Array of views containing the components of our dialog.
	private View[] mViews = new View[4];

	// Array of strings to populate the corresponding view.
	private String[] mStrings = new String[] { "", "", "", "" };

	private int iconId;

	private boolean listTitleStyle;

	// Array of linear layouts containing the 2 possible arrangements required
	// in case stacking needed.
	private LinearLayout[] mButtonContainers = new LinearLayout[2];

	// Theme containing the theme being used.
	private Theme mTheme = Theme.LIGHT;

	// Integers containing the hex colours to be used on the views.
	private int mPositiveColour, mNegativeColour, mTitleColour, mContentColour;

	// Positive background
	Drawable mPositiveBackground;

	// Integers containing the text sizes
	private int[] mTextSizes = new int[4];

	// Alignment for title to use
	private Alignment mTitleAlignment = Alignment.LEFT;

	// Alignment for content to use
	private Alignment mContentAlignment = Alignment.LEFT;

	// Alignment for button to use
	private Alignment mButtonsAlignment = Alignment.RIGHT;

	// boolean containing whether or not its intended to be right to left.
	private final boolean RTL;

	// We make our constructor private so we can only create it through the
	// builder inner class.
	private CustomDialog(Builder _builder) {

		// Call the super class to create our new dialog.
		super(new ContextThemeWrapper(_builder.mContext, _builder.mDarkTheme ? R.style.Dark : R.style.Light));

		// Apply the things from the builder.
		this.mContext = _builder.mContext;
		this.mTheme = _builder.mDarkTheme ? Theme.DARK : Theme.LIGHT;
		this.mStrings[0] = _builder.mTitle;
		this.mStrings[1] = _builder.mMessage;
		this.mStrings[2] = _builder.mPositiveText;
		this.mStrings[3] = _builder.mNegativeText;
		this.mPositiveColour = _builder.mPositiveColour;
		this.mNegativeColour = _builder.mNegativeColour;
		this.mTitleColour = _builder.mTitleColour;
		this.mContentColour = _builder.mContentColour;
		this.mTitleAlignment = _builder.mTitleAlignment;
		this.mTextSizes[0] = _builder.mTitleTextSize;
		this.mTextSizes[1] = _builder.mContentTextSize;
		this.mTextSizes[2] = _builder.mButtonTextSize;
		this.mTextSizes[3] = _builder.mButtonTextSize;
		this.mContentAlignment = _builder.mContentAlignment;
		this.mButtonsAlignment = _builder.mButtonsAlignment;
		this.RTL = _builder.RTL;
		this.mPositiveBackground = _builder.mPositiveBackground;
		this.iconId = _builder.iconId;
		this.setCancelable(_builder.mCancelable);

		mPositiveCallback = _builder.positiveClickListener;
		mNegativeCallback = _builder.negativeClickListener;

		mlistCallbacks = _builder.listClicklistener;

		if (_builder.mAdapter != null) {
			mCustomListAdapter = _builder.mAdapter;
			listTitleStyle = _builder.listTitleStyle != null ? _builder.listTitleStyle : true;
		} else if (_builder.mItems != null) {
			CustomListAdapter customListAdapter = new CustomListAdapter(mContext, R.layout.item_dialog_list,
					_builder.mItems);

			customListAdapter.setItemColour(_builder.mItemColour != 0 ? _builder.mItemColour
					: (this.mTheme == Theme.DARK ? Color.parseColor(DarkColours.ITEM.mColour) : Color
							.parseColor(LightColours.ITEM.mColour)));
			customListAdapter.setItemAlignment(_builder.mItemAlignment);
			customListAdapter.setItemTextSize(_builder.mItemTextSize);

			mCustomListAdapter = customListAdapter;

			listTitleStyle = _builder.listTitleStyle != null ? _builder.listTitleStyle : true;
		} else {
			listTitleStyle = _builder.listTitleStyle != null ? _builder.listTitleStyle : false;
		}
		// Set up references to views and then set the view.
		init();

		// Sets the properties of the views.
		setViewProperties(mViews, mStrings);

		// Check's if the different button arrangement is needed.
		checkIfButtonStackingNeeded();

		// Sets the listeners for the buttons.
		setListeners();

		// Apply the correct theme based on users preference.
		applyTheme();

		if (_builder.mCustomView != null) {
			setCustomView(_builder.mCustomView);
		}
	}

	private void init() {
		// Reference root view by inflating the layout file.
		mRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom, null, false);

		// Reference the views.
		mViews[0] = mRootView.findViewById(R.id.dialog_custom_title);
		mViews[1] = mRootView.findViewById(R.id.dialog_custom_content);
		mViews[2] = mRootView.findViewById(R.id.dialog_custom_confirm);
		mViews[3] = mRootView.findViewById(R.id.dialog_custom_cancel);

		// Reference containers for the buttons.
		mButtonContainers[0] = (LinearLayout) mRootView.findViewById(R.id.dialog_custom_alongside_buttons);
		mButtonContainers[1] = (LinearLayout) mRootView.findViewById(R.id.dialog_custom_stacked_buttons);

		// Set alignment for buttons view.
		mButtonContainers[0].setGravity(getGravityFromAlignment(mButtonsAlignment) | Gravity.CENTER_VERTICAL);

		mButtonContainers[1].setGravity(getGravityFromAlignment(mButtonsAlignment) | Gravity.CENTER_VERTICAL);

		// Set alignment for title view.
		((TextView) mViews[0]).setGravity(getGravityFromAlignment(mTitleAlignment) | Gravity.CENTER_VERTICAL);

		// Set alignment for content view.
		((TextView) mViews[1]).setGravity(getGravityFromAlignment(mContentAlignment) | Gravity.CENTER_VERTICAL);

		listView = (ListView) mRootView.findViewById(R.id.dialog_custom_list);
		if (mCustomListAdapter != null) {
			listView.setAdapter(mCustomListAdapter);
			listView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.GONE);
		}

		if (listTitleStyle) {
			int padding = (int) convertToPx(8.0f);
			((TextView) mViews[0]).setPadding(padding, padding, padding, padding);

			int paddingContent = (int) convertToPx(16.0f);
			mRootView.setPadding(paddingContent, paddingContent, paddingContent, paddingContent);
		}

		// Set the view of our dialog with the one we've inflated.
		super.setView(mRootView);
	}

	private void setListeners() {
		// Set the listener that handles positive button clicks.
		mViews[2].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Call the method from our interface and dismiss.
				if (mPositiveCallback != null)
					mPositiveCallback.onClick(CustomDialog.this, BUTTON_POSITIVE);
				dismiss();
			}
		});

		// Set the listener that handles negative button clicks.
		mViews[3].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Call the method from our interface and dismiss.
				if (mNegativeCallback != null)
					mNegativeCallback.onClick(CustomDialog.this, BUTTON_NEGATIVE);
				dismiss();
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				// If there is a listener available call onListItemSelected.
				if (mlistCallbacks != null) {
					mlistCallbacks.onClick(CustomDialog.this, i);
				}
				dismiss();
			}
		});
	}

	private void checkIfButtonStackingNeeded() {

		// Check if the text on the button is too big for the button.
		boolean isStackingNeeded = false;
		boolean hasButtons = false;
		if (mViews[2].getVisibility() != View.GONE) {
			hasButtons = true;
			isStackingNeeded |= ((Button) mViews[2]).getPaint().measureText(((Button) mViews[2]).getText().toString()) > convertToPx(56);
		}
		if (mViews[3].getVisibility() != View.GONE) {
			hasButtons = true;
			isStackingNeeded |= ((Button) mViews[3]).getPaint().measureText(((Button) mViews[3]).getText().toString()) > convertToPx(56);
		}
		// Toggle visibility of the layouts based on whether switching is
		// needed.
		if (hasButtons) {
			mButtonContainers[0].setVisibility(isStackingNeeded ? View.GONE : View.VISIBLE);
			mButtonContainers[1].setVisibility(isStackingNeeded ? View.VISIBLE : View.GONE);
		} else {
			mButtonContainers[0].setVisibility(View.GONE);
			mButtonContainers[1].setVisibility(View.GONE);
		}

		// Now the data may have changed we need to re-reference the buttons.
		updateButtonReferences(isStackingNeeded);
	}

	private void updateButtonReferences(boolean _isStackingNeeded) {
		// Re-reference the buttons.
		mViews[2] = mRootView.findViewById(_isStackingNeeded ? R.id.dialog_custom_confirm_stacked
				: R.id.dialog_custom_confirm);
		mViews[3] = mRootView.findViewById(_isStackingNeeded ? R.id.dialog_custom_cancel_stacked
				: R.id.dialog_custom_cancel);

		// Apply the data to the newly referenced views.
		setViewProperties(mViews, mStrings);
	}

	private float convertToPx(float _dp) {
		// Convert any density pixel value to it's corresponding pixel value.
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _dp, mContext.getResources().getDisplayMetrics());
	}

	private void setViewProperties(View[] _view, String[] _text) {
		// Loop through the views passed in and set the correct data to that
		// view.

		boolean hasButton = false;

		for (int i = 0; i < _view.length; i++) {

			// Gets the index from the mViews array as _view array may be a
			// different ordering and size to mViews.
			int index = getIndexFromView(_view[i]);

			// Hide the view if there is no data for this view.
			mViews[index].setVisibility(TextUtils.isEmpty(_text[i]) ? View.GONE : View.VISIBLE);

			// Reference the new text correctly..
			mStrings[index] = _text[i];

			// If it's a button treat it as one rather than generic view.

			if (index >= 2) {
				Button button = (Button) mViews[index];
				if (TextUtils.isEmpty(mStrings[index])) {
					button.setVisibility(View.GONE);
				} else {
					button.setText(mStrings[index].toUpperCase());
					button.setVisibility(View.VISIBLE);
					hasButton = true;
				}
				button.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizes[index]);
			}
			// Otherwise treat view as a text view.
			else {
				TextView textView = (TextView) mViews[index];
				textView.setText(mStrings[index]);
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizes[index]);
			}
		}

		if (iconId != 0) {
			((TextView) mViews[0]).setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
			((TextView) mViews[0]).setCompoundDrawablePadding((int) convertToPx(4f));
		}

		if (!hasButton) {
			mButtonContainers[0].setVisibility(View.GONE);
			mButtonContainers[1].setVisibility(View.GONE);
		}

		// Swap position of positive button and negative button if right to left required.
		if (RTL) {
			((ViewGroup) mViews[3].getParent()).removeView(mViews[2]);
			((ViewGroup) mViews[3].getParent()).addView(mViews[2], 0);
		}
	}

	private int getIndexFromView(View _view) {
		// Gets the correct index of the passed in view from the main array of
		// views.
		for (int i = 0; i < mViews.length; i++) {
			if (mViews[i] == _view)
				return i;
		}
		return 0;
	}

	private void applyTheme() {
		// Apply the correct colours based on theme and user preference.
		((TextView) mViews[0]).setTextColor(this.mTitleColour != 0 ? mTitleColour : (mTheme == Theme.LIGHT ? Color
				.parseColor(LightColours.TITLE.mColour) : Color.parseColor(DarkColours.TITLE.mColour)));
		((TextView) mViews[1]).setTextColor(this.mContentColour != 0 ? mContentColour : (mTheme == Theme.LIGHT ? Color
				.parseColor(LightColours.CONTENT.mColour) : Color.parseColor(DarkColours.CONTENT.mColour)));
		((Button) mViews[2]).setTextColor(this.mPositiveColour != 0 ? mPositiveColour : (mTheme == Theme.LIGHT ? Color
				.parseColor(LightColours.BUTTON.mColour) : Color.parseColor(DarkColours.BUTTON.mColour)));
		((Button) mViews[3]).setTextColor(this.mNegativeColour != 0 ? mNegativeColour : (mTheme == Theme.LIGHT ? Color
				.parseColor(LightColours.BUTTON.mColour) : Color.parseColor(DarkColours.BUTTON.mColour)));

		if (null != mPositiveBackground) {
			mViews[2].setBackgroundDrawable(mPositiveBackground);
		}
	}

	public Button getPositiveButton() {
		return (Button) mViews[2];
	}

	public Button getNegativeButton() {
		return (Button) mViews[3];
	}

	@Override
	public ListView getListView() {
		return listView;
	}

	protected CustomDialog setCustomView(View _view) {
		// If there's a custom view present - remove it.

		ViewGroup parent = ((ViewGroup) mViews[0].getParent());

		if (mCustomView != null)
			parent.removeView(mCustomView);

		// Reference new custom view.
		mCustomView = _view;

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		layoutParams.weight = 1.0f;

		// Add it to the dialog.
		parent.addView(mCustomView, 2, layoutParams);
		return this;
	}

	public static class Builder {

		// Required fields for the dialog.
		private final Context mContext;
		private String mTitle, mPositiveText;

		private Boolean listTitleStyle;

		private int iconId;

		private DialogInterface.OnClickListener positiveClickListener, negativeClickListener;

		// View holding any custom views passed in.
		private View mCustomView;

		private String[] mItems;
		private Alignment mItemAlignment = Alignment.LEFT;
		private int mItemColour = 0, mItemTextSize = 18;
		private DialogInterface.OnClickListener listClicklistener;
		private ListAdapter mAdapter;

		// Builder constructor that all dialogs will be created through.
		public Builder(Context _context) {
			this.mContext = _context;

			mPositiveColour = mContext.getResources().getColor(android.R.color.holo_blue_dark);
		}

		// Optional parameters initialised by default.
		private String mNegativeText = "", mMessage = "";
		private int mPositiveColour = 0, mNegativeColour = 0, mTitleColour = 0, mContentColour = 0,
				mTitleTextSize = 22, mContentTextSize = 18, mButtonTextSize = 14;
		private boolean mDarkTheme = false;
		private boolean RTL = false;

		private Alignment mTitleAlignment = Alignment.LEFT;
		private Alignment mContentAlignment = Alignment.LEFT;
		private Alignment mButtonsAlignment = Alignment.RIGHT;
		private Drawable mPositiveBackground;
		private boolean mCancelable;

		public Context getContext() {
			return new ContextThemeWrapper(mContext, mDarkTheme ? R.style.Dark : R.style.Light);
		}

		public Builder setTitle(String mTitle) {
			this.mTitle = mTitle;
			return this;
		}

		public Builder setTitle(int mTitleId) {
			this.mTitle = mContext.getString(mTitleId);
			return this;
		}

		public Builder setListTitleStyle(boolean style) {
			listTitleStyle = style;
			return this;
		}

		public void setPositiveText(String mPositiveText) {
			this.mPositiveText = mPositiveText;
		}

		public void setPositiveText(int mPositiveTextID) {
			this.mPositiveText = mContext.getString(mPositiveTextID);
		}

		public Builder setMessage(int _contentId) {
			this.mMessage = mContext.getString(_contentId);
			return this;
		}

		public Builder setMessage(String _content) {
			this.mMessage = _content;
			return this;
		}

		public Builder negativeText(String _negativeText) {
			this.mNegativeText = _negativeText;
			return this;
		}

		public Builder negativeText(int _negativeTextResId) {
			this.mNegativeText = mContext.getString(_negativeTextResId);
			return this;
		}

		public Builder positiveColor(String _positiveColour) {
			this.mPositiveColour = Color.parseColor(_positiveColour);
			return this;
		}

		public Builder negativeColor(String _negativeColour) {
			this.mNegativeColour = Color.parseColor(_negativeColour);
			return this;
		}

		public Builder titleColor(String _colour) {
			this.mTitleColour = Color.parseColor(_colour);
			return this;
		}

		public Builder contentColor(String _colour) {
			this.mContentColour = Color.parseColor(_colour);
			return this;
		}

		public Builder positiveColor(int _positiveColour) {
			this.mPositiveColour = _positiveColour;
			return this;
		}

		public Builder negativeColor(int _negativeColour) {
			this.mNegativeColour = _negativeColour;
			return this;
		}

		public Builder titleColor(int _colour) {
			this.mTitleColour = _colour;
			return this;
		}

		public View setView(int layoutId) {
			mCustomView = LayoutInflater.from(getContext()).inflate(layoutId, null, false);
			return mCustomView;
		}

		public void setView(View mCustomView) {
			this.mCustomView = mCustomView;
		}

		public Builder contentColor(int _colour) {
			this.mContentColour = _colour;
			return this;
		}

		public Builder positiveColorRes(int _positiveColour) {
			this.mPositiveColour = mContext.getResources().getColor(_positiveColour);
			return this;
		}

		public Builder negativeColorRes(int _negativeColour) {
			this.mNegativeColour = mContext.getResources().getColor(_negativeColour);
			return this;
		}

		public Builder titleColorRes(int _colour) {
			this.mTitleColour = mContext.getResources().getColor(_colour);
			return this;
		}

		public Builder contentColorRes(int _colour) {
			this.mContentColour = mContext.getResources().getColor(_colour);
			return this;
		}

		public Builder titleTextSize(int _textSize) {
			this.mTitleTextSize = _textSize;
			return this;
		}

		public Builder contentTextSize(int _textSize) {
			this.mContentTextSize = _textSize;
			return this;
		}

		public Builder buttonTextSize(int _textSize) {
			this.mButtonTextSize = _textSize;
			return this;
		}

		public Builder setDarkTheme(boolean _isDark) {
			this.mDarkTheme = _isDark;
			return this;
		}

		public Builder titleAlignment(Alignment _alignment) {
			this.mTitleAlignment = _alignment;
			return this;
		}

		public Builder contentAlignment(Alignment _alignment) {
			this.mContentAlignment = _alignment;
			return this;
		}

		public Builder buttonAlignment(Alignment _alignment) {
			this.mButtonsAlignment = _alignment;
			return this;
		}

		public Builder rightToLeft(boolean _rightToLeft) {
			this.RTL = _rightToLeft;
			if (_rightToLeft) {
				this.mTitleAlignment = Alignment.RIGHT;
				this.mItemAlignment = Alignment.RIGHT;
				this.mContentAlignment = Alignment.RIGHT;
				this.mButtonsAlignment = Alignment.LEFT;
			}
			return this;
		}

		public Builder positiveBackground(Drawable _positiveBkgd) {
			this.mPositiveBackground = _positiveBkgd;
			return this;
		}

		public Builder positiveBackground(int _positiveBkgd) {
			this.mPositiveBackground = mContext.getResources().getDrawable(_positiveBkgd);
			return this;
		}

		public CustomDialog create() {
			return new CustomDialog(this);
		}

		public CustomDialog show() {
			CustomDialog customDialog = create();
			customDialog.show();

			return customDialog;
		}

		public void setNegativeButton(String cancel, DialogInterface.OnClickListener onClickListener) {
			mNegativeText = cancel;
			negativeClickListener = onClickListener;

		}

		public void setNegativeButton(int cancel, DialogInterface.OnClickListener onClickListener) {
			mNegativeText = mContext.getString(cancel);
			negativeClickListener = onClickListener;
		}

		public void setPositiveButton(String ok, DialogInterface.OnClickListener onClickListener) {
			mPositiveText = ok;
			positiveClickListener = onClickListener;
		}

		public void setPositiveButton(int ok, DialogInterface.OnClickListener onClickListener) {
			mPositiveText = mContext.getString(ok);
			positiveClickListener = onClickListener;

		}

		public void setCancelable(boolean b) {
			this.mCancelable = b;
		}

		public void setAdapter(ListAdapter mAdapter, DialogInterface.OnClickListener listClicklistener) {
			this.mAdapter = mAdapter;
			this.listClicklistener = listClicklistener;
		}

		public void setItems(int arrayId, DialogInterface.OnClickListener onClickListener) {
			this.mItems = mContext.getResources().getStringArray(arrayId);
			this.listClicklistener = onClickListener;
		}

		public void setItems(String[] array, DialogInterface.OnClickListener onClickListener) {
			mItems = array;
			listClicklistener = onClickListener;
		}

		public Builder itemAlignment(Alignment _alignment) {
			this.mItemAlignment = _alignment;
			return this;
		}

		public Builder itemColor(String _colour) {
			this.mItemColour = Color.parseColor(_colour);
			return this;
		}

		public Builder itemColor(int _colour) {
			this.mItemColour = _colour;
			return this;
		}

		public Builder itemColorRes(int _colour) {
			this.mItemColour = mContext.getResources().getColor(_colour);
			return this;
		}

		public Builder itemTextSize(int _textSize) {
			this.mItemTextSize = _textSize;
			return this;
		}

		public void setIcon(int iconId) {
			this.iconId = iconId;
		}

	}
}
