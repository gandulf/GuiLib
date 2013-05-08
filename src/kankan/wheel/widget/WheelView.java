/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget;

import kankan.wheel.widget.adapters.WheelViewAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.gandulf.guilib.R;
import com.gandulf.guilib.util.Debug;

/**
 * Numeric wheel view.
 * 
 * @author Yuri Kanivets
 */
public class WheelView extends View {

	/**
	 * Synonym for {@link MotionEvent#ACTION_MASK}.
	 */
	public static final int ACTION_MASK = 0xff;

	/** Top and bottom shadows colors */
	private static final int[] SHADOWS_COLORS = new int[] { 0xFF111111, 0x00AAAAAA, 0x00AAAAAA };

	/** Top and bottom items offset (to hide that) */
	private static final int ITEM_OFFSET_PERCENT = 10;

	/** Left and right padding value */
	private static final int PADDING = 0;

	/** Default count of visible items */
	private static final int DEF_VISIBLE_ITEMS = 5;

	public static final int HORIZONTAL = android.widget.LinearLayout.HORIZONTAL;
	public static final int VERTICAL = android.widget.LinearLayout.VERTICAL;

	// Wheel Values
	private int currentItem = 0;

	// Count of visible items
	private int visibleItems = DEF_VISIBLE_ITEMS;

	// Item height or width depending on orientation
	private int itemSize = 0;

	// Center Line
	private Drawable centerDrawable;

	// Shadows drawables
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	// Scrolling
	private WheelScroller scroller;
	private boolean isScrollingPerformed;
	private int scrollingOffset;

	// Cyclic
	boolean isCyclic = false;

	// Items layout
	private LinearLayout itemsLayout;

	// The number of first item in layout
	private int firstItem;

	// View adapter
	private WheelViewAdapter viewAdapter;

	// Recycle
	private WheelRecycle recycle = new WheelRecycle(this);

	// orientation HORIZONTAL OR VERTICAL
	private int orientation = VERTICAL;

	// Listeners
	private OnWheelChangedListener onWheelChangedListeners;
	private OnWheelScrollListener onWheelScrolledListeners;
	private OnWheelClickedListener onWheelClickedListeners;

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context, attrs);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context, attrs);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context) {
		super(context);
		initData(context, null);
	}

	/**
	 * Initializes class data
	 * 
	 * @param context
	 *            the context
	 */
	private void initData(Context context, AttributeSet attrs) {
		if (!isInEditMode()) {
			scroller = new WheelScroller(getContext(), scrollingListener, orientation);
		}

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);

		int vis = a.getInt(R.styleable.WheelView_visibleItems, -1);

		if (vis != -1)
			setVisibleItems(vis);

		String orientation = a.getString(R.styleable.WheelView_wheelOrientation);
		if ("vertical".equals(orientation))
			setOrientation(VERTICAL);
		else if ("horizontal".equals(orientation))
			setOrientation(HORIZONTAL);

		// TypedArray a = context.obtainStyledAttributes(attrs,
		// R.styleable.WheelView);
		//
		// int index = a.getInt(R.styleable.WheelView_orientation, -1);
		// if (index >= 0) {
		// setOrientation(index);
		// }
	}

	// Scrolling listener
	WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		public void onScroll(int distance) {
			doScroll(distance);

			if (orientation == VERTICAL) {
				int height = getHeight();
				if (scrollingOffset > height) {
					scrollingOffset = height;
					scroller.stopScrolling();
				} else if (scrollingOffset < -height) {
					scrollingOffset = -height;
					scroller.stopScrolling();
				}
			} else {
				int height = getWidth();
				if (scrollingOffset > height) {
					scrollingOffset = height;
					scroller.stopScrolling();
				} else if (scrollingOffset < -height) {
					scrollingOffset = -height;
					scroller.stopScrolling();
				}
			}
		}

		public void onFinished() {
			if (isScrollingPerformed) {
				isScrollingPerformed = false;
				notifyChangingListeners(-1, currentItem);
			}

			scrollingOffset = 0;
			invalidate();
		}

		public void onJustify() {
			if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
				scroller.scroll(scrollingOffset, 0);
				notifyChangingListeners(-1, currentItem);
			}
		}
	};

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		itemSize = 0;
		if (scroller != null)
			scroller.setOrientation(orientation);
		if (itemsLayout != null)
			itemsLayout.setOrientation(orientation);
		invalidateWheel(true);
	}

	/**
	 * Set the the specified scrolling interpolator
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.setInterpolator(interpolator);
	}

	/**
	 * Gets count of visible items
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets the desired count of visible items. Actual amount of visible items
	 * depends on wheel layout parameters. To apply changes and rebuild view
	 * call measure().
	 * 
	 * @param count
	 *            the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * Gets view adapter
	 * 
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter() {
		return viewAdapter;
	}

	// Adapter listener
	private DataSetObserver dataObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			invalidateWheel(false);
		}

		@Override
		public void onInvalidated() {
			invalidateWheel(true);
		}
	};

	/**
	 * Sets view adapter. Usually new adapters contain different views, so it
	 * needs to rebuild view by calling measure().
	 * 
	 * @param viewAdapter
	 *            the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter) {
		if (this.viewAdapter != null) {
			this.viewAdapter.unregisterDataSetObserver(dataObserver);
		}
		this.viewAdapter = viewAdapter;
		if (this.viewAdapter != null) {
			this.viewAdapter.registerDataSetObserver(dataObserver);
		}

		invalidateWheel(true);
	}

	/**
	 * Notifies changing listeners
	 * 
	 * @param oldPosition
	 *            the old wheel value
	 * @param newPosition
	 *            the new wheel value
	 */
	protected void notifyChangingListeners(final int oldPosition, final int newPosition) {
		if (onWheelChangedListeners != null) {
			post(new Runnable() {

				@Override
				public void run() {
					onWheelChangedListeners.onWheelChanged(WheelView.this, oldPosition, newPosition);
				}
			});
		}
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		if (onWheelScrolledListeners != null)
			onWheelScrolledListeners.onScrollingStarted(this);

	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		if (onWheelScrolledListeners != null)
			onWheelScrolledListeners.onScrollingFinished(this);

	}

	/**
	 * Notifies listeners about clicking
	 */
	protected void notifyClickListenersAboutClick(int position) {
		if (onWheelClickedListeners != null) {
			if (isSoundEffectsEnabled())
				playSoundEffect(SoundEffectConstants.CLICK);
			onWheelClickedListeners.onWheelClick(this, position);
		}

	}

	public OnWheelChangedListener getOnWheelChangedListeners() {
		return onWheelChangedListeners;
	}

	public void setOnWheelChangedListeners(OnWheelChangedListener onWheelChangedListeners) {
		this.onWheelChangedListeners = onWheelChangedListeners;
	}

	public OnWheelScrollListener getOnWheelScrolledListeners() {
		return onWheelScrolledListeners;
	}

	public void setOnWheelScrolledListeners(OnWheelScrollListener onWheelScrolledListeners) {
		this.onWheelScrolledListeners = onWheelScrolledListeners;
	}

	public OnWheelClickedListener getOnWheelClickedListeners() {
		return onWheelClickedListeners;
	}

	public void setOnWheelClickedListeners(OnWheelClickedListener onWheelClickedListeners) {
		this.onWheelClickedListeners = onWheelClickedListeners;
	}

	/**
	 * Gets current value
	 * 
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 * 
	 * @param index
	 *            the item index
	 * @param animated
	 *            the animation flag
	 */
	public void setCurrentItem(int index, boolean animated, boolean notify) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return; // throw?
		}

		int itemCount = viewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;
			} else {
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {
				int itemsToScroll = index - currentItem;
				if (isCyclic) {
					int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
					if (scroll < Math.abs(itemsToScroll)) {
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}
				scroll(itemsToScroll, 0);
			} else {
				scrollingOffset = 0;

				int old = currentItem;
				currentItem = index;
				if (notify)
					notifyChangingListeners(old, currentItem);

				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * 
	 * @param index
	 *            the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false, true);
	}

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown
	 * the last one
	 * 
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * 
	 * @param isCyclic
	 *            the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}

	/**
	 * Invalidates wheel
	 * 
	 * @param clearCaches
	 *            if true then cached views will be clear
	 */
	public void invalidateWheel(boolean clearCaches) {
		if (clearCaches) {
			recycle.clearAll();
			if (itemsLayout != null) {
				itemsLayout.removeAllViews();
			}
			scrollingOffset = 0;
		} else if (itemsLayout != null) {
			// cache all items
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		}

		if (viewAdapter != null) {
			if (currentItem >= viewAdapter.getItemsCount())
				setCurrentItem(viewAdapter.getItemsCount() - 1);
		}
		invalidate();
	}

	/**
	 * Initializes resources
	 */
	private void initResourcesIfNecessary() {

		if (centerDrawable == null) {
			if (orientation == VERTICAL)
				centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
			else
				centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val_horizontal);
		}

		if (orientation == VERTICAL) {
			if (topShadow == null) {
				topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
			}

			if (bottomShadow == null) {
				bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
			}
			setBackgroundResource(R.drawable.wheel_bg);
		} else {
			if (topShadow == null) {
				topShadow = new GradientDrawable(Orientation.LEFT_RIGHT, SHADOWS_COLORS);
			}

			if (bottomShadow == null) {
				bottomShadow = new GradientDrawable(Orientation.RIGHT_LEFT, SHADOWS_COLORS);
			}
			setBackgroundResource(R.drawable.wheel_bg_horizontal);
		}
	}

	/**
	 * Calculates desired height for layout
	 * 
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemSize = layout.getChildAt(0).getMeasuredHeight();
		}

		int desired = itemSize * visibleItems - itemSize * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumHeight());
	}

	private int getDesiredWidth(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemSize = layout.getChildAt(0).getMeasuredWidth();
		}

		int desired = itemSize * visibleItems - itemSize * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumWidth());
	}

	/**
	 * Returns height of wheel item
	 * 
	 * @return the item height
	 */
	private int getItemSize() {

		// sometimes during measurement values get way out of bounds, clear them
		// here so we do not work with this cached wrong value
		if (itemSize > 10000 || itemSize < 10)
			itemSize = 0;

		if (itemSize != 0) {
			return itemSize;
		}
		int containerSize;
		if (orientation == VERTICAL) {

			if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
				itemSize = itemsLayout.getChildAt(0).getHeight();

			}

			if (itemSize == 0) {
				containerSize = getHeight() != 0 ? getHeight() : getMeasuredHeight();

				return containerSize / visibleItems;
			}

		} else {
			if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
				int count = itemsLayout.getChildCount();
				for (int i = 0; i < count; i++) {
					itemSize = Math.max(itemSize, itemsLayout.getChildAt(i).getWidth());
				}

			}

			if (itemSize == 0) {
				containerSize = getWidth() != 0 ? getWidth() : getMeasuredWidth();

				return containerSize / visibleItems;
			}

		}

		return itemSize;
	}

	/**
	 * Calculates control width and creates text layouts
	 * 
	 * @param widthSize
	 *            the input layout width
	 * @param mode
	 *            the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		int width = itemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}

		itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return width;
	}

	/**
	 * Calculates control width and creates text layouts
	 * 
	 * @param heightSize
	 *            the input layout width
	 * @param mode
	 *            the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutHeight(int heightSize, int mode) {
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
		int height = itemsLayout.getMeasuredHeight();

		if (mode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height += 2 * PADDING;

			// Check against our minimum width
			height = Math.max(height, getSuggestedMinimumHeight());

			if (mode == MeasureSpec.AT_MOST && heightSize < height) {
				height = heightSize;
			}
		}

		itemsLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(height - 2 * PADDING, MeasureSpec.EXACTLY));

		return height;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		buildViewForMeasuring();

		if (orientation == VERTICAL) {
			int width = calculateLayoutWidth(widthSize, widthMode);

			int height;
			if (heightMode == MeasureSpec.EXACTLY) {
				height = heightSize;
			} else {
				height = getDesiredHeight(itemsLayout);

				if (heightMode == MeasureSpec.AT_MOST) {
					height = Math.min(height, heightSize);
				}
			}

			setMeasuredDimension(width, height);
		} else {
			int height = calculateLayoutHeight(heightSize, heightMode);

			int width;
			if (widthMode == MeasureSpec.EXACTLY) {
				width = widthSize;
			} else {
				width = getDesiredWidth(itemsLayout);

				if (widthMode == MeasureSpec.AT_MOST) {
					width = Math.min(width, widthSize);
				}
			}

			setMeasuredDimension(width, height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layout(r - l, b - t);
	}

	/**
	 * Sets layouts width and height
	 * 
	 * @param width
	 *            the layout width
	 * @param height
	 *            the layout height
	 */
	private void layout(int width, int height) {
		// itemSize = 0;
		if (orientation == VERTICAL)
			itemsLayout.layout(0, 0, width - 2 * PADDING, height);
		else
			itemsLayout.layout(0, 0, width, height - 2 * PADDING);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
			updateView();

			drawItems(canvas);
			drawCenterRect(canvas);
		}

		drawShadows(canvas);
	}

	/**
	 * Draws shadows on top and bottom of control
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		if (orientation == VERTICAL) {
			int height;
			if (isEnabled()) {
				height = (int) (1.5 * getItemSize());
			} else {
				height = (int) ((visibleItems) / 2 * getItemSize());
			}
			topShadow.setBounds(0, 0, getWidth(), height);

			topShadow.draw(canvas);

			bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
			bottomShadow.draw(canvas);
		} else {
			int width;
			if (isEnabled()) {
				width = (int) (1.5 * getItemSize());
			} else {
				width = (int) ((visibleItems) / 2 * getItemSize());
			}

			topShadow.setBounds(0, 0, width, getHeight());
			topShadow.draw(canvas);

			bottomShadow.setBounds(getWidth() - width, 0, getWidth(), getHeight());
			bottomShadow.draw(canvas);
		}
	}

	/**
	 * Draws items
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		canvas.save();

		if (orientation == VERTICAL) {
			int top = (currentItem - firstItem) * getItemSize() + (getItemSize() - getHeight()) / 2;
			canvas.translate(PADDING, -top + scrollingOffset);
		} else {
			int left = (currentItem - firstItem) * getItemSize() + (getItemSize() - getWidth()) / 2;
			canvas.translate(-left + scrollingOffset, PADDING);
		}

		itemsLayout.draw(canvas);

		canvas.restore();
	}

	/**
	 * Draws rect for current value
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		if (orientation == VERTICAL) {
			int center = getHeight() / 2;
			int offset = (int) (getItemSize() / 2 * 1.2);
			centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
			centerDrawable.draw(canvas);
		} else {
			int center = getWidth() / 2;
			int offset = (int) (getItemSize() / 2 * 1.2);
			centerDrawable.setBounds(center - offset, 0, center + offset, getHeight());
			centerDrawable.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		final int action = event.getAction() & ACTION_MASK;

		if (getParent() != null && orientation == HORIZONTAL) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		switch (action) {

		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if (!isScrollingPerformed) {
				int distance, items;

				if (orientation == VERTICAL) {
					distance = (int) event.getY() - getHeight() / 2;
					if (distance > 0) {
						distance += getItemSize() / 2;
					} else {
						distance -= getItemSize() / 2;
					}
					items = distance / getItemSize();
				} else {
					distance = (int) event.getX() - getWidth() / 2;
					if (distance > 0) {
						distance += getItemSize() / 2;
					} else {
						distance -= getItemSize() / 2;
					}
					items = distance / getItemSize();
				}

				if (isValidItemIndex(currentItem + items)) {
					setCurrentItem(currentItem + items, true, true);
				}
				if (items == 0) {
					notifyClickListenersAboutClick(currentItem);
				}

			}
			break;
		}

		return scroller.onTouchEvent(event);

	}

	/**
	 * Scrolls the wheel
	 * 
	 * @param delta
	 *            the scrolling value
	 */
	private void doScroll(int delta) {
		scrollingOffset += delta;

		int itemSize = getItemSize();
		int count = scrollingOffset / itemSize;
		int pos = currentItem - count;
		int itemCount = viewAdapter.getItemsCount();

		int fixPos = scrollingOffset % itemSize;
		if (Math.abs(fixPos) <= itemSize / 2) {
			fixPos = 0;
		}
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) {
				pos--;
				count++;
			} else if (fixPos < 0) {
				pos++;
				count--;
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount;
			}
			pos %= itemCount;
		} else {
			//
			if (pos < 0) {
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {
				count = currentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) {
				pos++;
				count--;
			}
		}

		int offset = scrollingOffset;
		if (pos != currentItem) {
			setCurrentItem(pos, false, false);
		} else {
			invalidate();
		}

		// update offset
		scrollingOffset = offset - count * itemSize;
		if (orientation == VERTICAL) {
			if (scrollingOffset > getHeight()) {
				scrollingOffset = scrollingOffset % getHeight() + getHeight();
			}
		} else {
			if (scrollingOffset > getWidth()) {
				scrollingOffset = scrollingOffset % getWidth() + getWidth();
			}
		}
	}

	/**
	 * Scroll the wheel
	 * 
	 * @param itemsToSkip
	 *            items to scroll
	 * @param time
	 *            scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemSize() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items
	 * 
	 * @return the items range
	 */
	private ItemsRange getItemsRange() {
		if (getItemSize() == 0) {
			return null;
		}

		int first = currentItem;
		int count = 1;

		int containerSize = orientation == VERTICAL ? getHeight() : getWidth();

		while (count * getItemSize() < containerSize) {
			first--;
			count += 2; // top + bottom items
		}

		if (scrollingOffset != 0) {
			if (scrollingOffset > 0) {
				first--;
			}
			count++;

			// process empty items above the first or below the second
			int emptyItems = scrollingOffset / getItemSize();
			first -= emptyItems;
			count += Math.asin(emptyItems);
		}

		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 * 
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		boolean updated = false;
		ItemsRange range = getItemsRange();
		if (itemsLayout != null) {
			int first = recycle.recycleItems(itemsLayout, firstItem, range);
			updated = firstItem != first;
			firstItem = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
		}

		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			firstItem = range.getFirst();
		}

		int first = firstItem;
		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
				first++;
			}
		}
		firstItem = first;

		return updated;
	}

	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items
	 * sizes.
	 */
	private void updateView() {
		if (rebuildItems()) {

			if (orientation == VERTICAL)
				calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			else
				calculateLayoutHeight(getHeight(), MeasureSpec.EXACTLY);

			layout(getWidth(), getHeight());

			Debug.verbose("Wheelview updateView causes Layout");
		}
	}

	/**
	 * Creates item layouts if necessary
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());
			itemsLayout.setOrientation(orientation);
		}
	}

	/**
	 * Builds view for measuring
	 */
	private void buildViewForMeasuring() {
		// clear all items
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		// add views
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
	}

	/**
	 * Adds view for item to items layout
	 * 
	 * @param index
	 *            the item index
	 * @param first
	 *            the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		View view = getItemView(index);

		if (view != null) {
			if (view.getLayoutParams() == null)
				view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			if (orientation == VERTICAL) {
				view.getLayoutParams().width = LayoutParams.MATCH_PARENT;
				view.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			} else {
				view.getLayoutParams().width = getItemSize();
				view.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			}
			// ((LinearLayout.LayoutParams) view.getLayoutParams()).weight = 1;

			if (first) {
				itemsLayout.addView(view, 0);
			} else {
				itemsLayout.addView(view);
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks whether intem index is valid
	 * 
	 * @param index
	 *            the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return viewAdapter != null && viewAdapter.getItemsCount() > 0
				&& (isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
	}

	/**
	 * Returns view for specified item
	 * 
	 * @param index
	 *            the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}
		int count = viewAdapter.getItemsCount();
		if (!isValidItemIndex(index)) {
			return viewAdapter.getEmptyItem(index, recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}
}
