package com.ecloud.pulltozoomview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.gandulf.guilib.R;

/**
 * Author: ZhuWenWu Version V1.0 Date: 2014/9/1 10:50. Description: Modification History: Date Author Version
 * Description ----------------------------------------------------------------------------------- 2014/9/1 ZhuWenWu 1.0
 * 1.0 Why & What is modified:
 */
public class PullToZoomScrollView extends ScrollView {
	private static final String TAG = PullToZoomScrollView.class.getSimpleName();

	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float paramAnonymousFloat) {
			float f = paramAnonymousFloat - 1.0F;
			return 1.0F + f * (f * (f * (f * f)));
		}
	};

	private View mContentView;
	private View mHeadView;
	private View mZoomView;

	private ViewGroup mContentContainer;
	private ViewGroup mHeaderContainer;
	private ViewGroup mZoomContainer;
	private LinearLayout mRootContainer;

	private OnScrollViewChangedListener mOnScrollListener;
	private ScalingRunnable mScalingRunnable;

	private int mScreenHeight;
	private int mZoomHeight;
	private int mZoomWidth;

	private int mActivePointerId = -1;
	private float mLastMotionY = -1.0F;
	private float mLastScale = -1.0F;
	private float mMaxScale = -1.0F;
	private boolean isHeaderTop = true;
	private boolean isEnableZoom = true;
	private boolean isParallax = false;

	private boolean mIsOverScrollEnabled = true;

	public PullToZoomScrollView(Context context) {
		this(context, null);
	}

	public PullToZoomScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullToZoomScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		mHeaderContainer = new FrameLayout(getContext());
		mZoomContainer = new FrameLayout(getContext());
		mContentContainer = new FrameLayout(getContext());

		mRootContainer = new LinearLayout(getContext());
		mRootContainer.setOrientation(LinearLayout.VERTICAL);

		if (attrs != null) {
			LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
			//
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToZoomScrollView);

			int zoomViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollZoomView, 0);
			if (zoomViewResId > 0) {
				mZoomView = mLayoutInflater.inflate(zoomViewResId, mZoomContainer, false);
				mZoomContainer.addView(mZoomView);
				mHeaderContainer.addView(mZoomContainer);

				// move height from zoomview to container
				mZoomContainer.getLayoutParams().height = mZoomView.getLayoutParams().height;
				mZoomView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			}

			int headViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollHeadView, 0);
			if (headViewResId > 0) {
				mHeadView = mLayoutInflater.inflate(headViewResId, mHeaderContainer, false);
				mHeaderContainer.addView(mHeadView);
			}
			int contentViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollContentView, 0);
			if (contentViewResId > 0) {
				mContentView = mLayoutInflater.inflate(contentViewResId, mContentContainer, false);
				mContentContainer.addView(mContentView);
			}

			a.recycle();
		}

		if (!this.isInEditMode()) {
			DisplayMetrics localDisplayMetrics = new DisplayMetrics();
			((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
			mScreenHeight = localDisplayMetrics.heightPixels;
			mZoomWidth = localDisplayMetrics.widthPixels;
		}
		mScalingRunnable = new ScalingRunnable();

		mRootContainer.addView(mHeaderContainer);
		mRootContainer.addView(mContentContainer);

		mRootContainer.setClipChildren(false);
		mHeaderContainer.setClipChildren(false);

		addView(mRootContainer);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public void setEnableZoom(boolean isEnableZoom) {
		this.isEnableZoom = isEnableZoom;
	}

	public void setParallax(boolean isParallax) {
		this.isParallax = isParallax;
	}

	public void setOnScrollListener(OnScrollViewChangedListener mOnScrollListener) {
		this.mOnScrollListener = mOnScrollListener;
	}

	public void setContentContainerView(View view) {
		if (mContentContainer != null) {
			mContentContainer.removeAllViews();
			mContentView = view;
			mContentContainer.addView(view);
		}
	}

	public void setHeaderContainer(View view) {
		if (mHeaderContainer != null && view != null) {
			mHeaderContainer.removeAllViews();
			mHeadView = view;
			if (mZoomView != null && mZoomContainer != null) {
				mZoomContainer.removeAllViews();
				mZoomContainer.addView(mZoomView);
				mHeaderContainer.addView(mZoomContainer);
			}
			mHeaderContainer.addView(mHeadView);
		}
	}

	public void setZoomView(View view) {
		if (mZoomContainer != null && view != null) {
			this.mZoomView = view;
			mZoomContainer.removeAllViews();
			mZoomContainer.addView(mZoomView);
			if (mHeaderContainer != null) {
				mHeaderContainer.removeAllViews();
				mHeaderContainer.addView(mZoomContainer);
				if (mHeadView != null) {
					mHeaderContainer.addView(mHeadView);
				}
			}
		}
	}

	public void showHeaderView() {
		if (mZoomView != null || mHeadView != null) {
			mRootContainer.addView(mHeaderContainer, 0);
		}
	}

	public void hideHeaderView() {
		if (mZoomView != null || mHeadView != null) {
			mRootContainer.removeView(mHeaderContainer);
		}
	}

	public ViewGroup getZoomContainer() {
		return mZoomContainer;
	}

	public View getZoomView() {
		return mZoomView;
	}

	public View getContentView() {
		return mContentView;
	}

	public View getHeadView() {
		return mHeadView;
	}

	public void setZoomHeight(int mZoomHeight) {
		this.mZoomHeight = mZoomHeight;
	}

	public LinearLayout getRootContainer() {
		return mRootContainer;
	}

	private void scaleDown() {
		if (mZoomContainer.getBottom() >= mZoomHeight) {
			Log.d(TAG, "scaleDown");
			float targetScale = 1.0f;
			mScalingRunnable.startAnimation(targetScale, 200L);
		}
	}

	private void scaleUp() {
		mScalingRunnable.startAnimation(mMaxScale, 200L);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = MotionEventCompat.getActionMasked(event);
		// Log.d(TAG, "onInterceptTouchEvent --> action = " + action);
		if (isHeaderTop && isEnableZoom) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_OUTSIDE:
				if (!mScalingRunnable.isFinished()) {
					mScalingRunnable.abortAnimation();
				}
				mLastMotionY = event.getY();
				mActivePointerId = MotionEventCompat.getPointerId(event, 0);
				mMaxScale = (mScreenHeight / mZoomHeight);
				mLastScale = (mZoomContainer.getBottom() / mZoomHeight);
				break;
			}
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mZoomHeight == 0) {
			if (mZoomContainer != null) {
				mZoomHeight = mZoomContainer.getHeight();
				mZoomWidth = mZoomContainer.getWidth();
				mMaxScale = (mScreenHeight / mZoomHeight);
			}
		}
	}

	@Override
	protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
		super.onScrollChanged(left, top, oldLeft, oldTop);
		if (isEnableZoom) {
			isHeaderTop = getScrollY() <= 0;

			if (isParallax) {
				float f = mZoomHeight - mZoomContainer.getBottom() + getScrollY();
				Log.d(TAG, "f = " + f);
				if ((f > 0.0F) && (f < mZoomHeight)) {
					int i = (int) (0.65D * f);
					mHeaderContainer.scrollTo(0, -i);
				} else if (mHeaderContainer.getScrollY() != 0) {
					mHeaderContainer.scrollTo(0, 0);
				}
			}
		}
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(left, top, oldLeft, oldTop);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = MotionEventCompat.getActionMasked(ev);
		// Log.d(TAG, "onTouchEvent --> action = " + action);
		if (isHeaderTop && isEnableZoom) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_OUTSIDE:
				if (!mScalingRunnable.isFinished()) {
					mScalingRunnable.abortAnimation();
				}
				mLastMotionY = ev.getY();
				mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
				mMaxScale = (mScreenHeight / mZoomHeight);
				mLastScale = (mZoomContainer.getBottom() / mZoomHeight);
				break;
			case MotionEvent.ACTION_MOVE:
				// Log.d(TAG, "mActivePointerId = " + mActivePointerId);
				int j = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
				if (j == -1) {
					Log.e(TAG, "Invalid pointerId = " + mActivePointerId + " in onTouchEvent");
				} else {
					if (mLastMotionY == -1.0F) {
						mLastMotionY = MotionEventCompat.getY(ev, j);
					}
					if (mZoomContainer.getBottom() >= mZoomHeight) {
						float f = ((ev.getY(j) - mLastMotionY + mZoomContainer.getBottom()) / mZoomHeight - mLastScale)
								/ 2.0F + mLastScale;

						if (!setZoomScale(f)) {
							return super.onTouchEvent(ev);
						}
						mLastMotionY = MotionEventCompat.getY(ev, j);
						return true;
					}
					this.mLastMotionY = MotionEventCompat.getY(ev, j);
				}
				break;
			case MotionEvent.ACTION_UP:
				reset();
				scaleDown();
				break;
			case MotionEvent.ACTION_CANCEL:
				int i = MotionEventCompat.getActionIndex(ev);
				if (i >= 0) {
					mLastMotionY = MotionEventCompat.getY(ev, i);
					mActivePointerId = MotionEventCompat.getPointerId(ev, i);
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				onSecondaryPointerUp(ev);
				int idx = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
				if (idx >= 0) {
					mLastMotionY = MotionEventCompat.getY(ev, idx);
				}
				break;
			}
		}
		return super.onTouchEvent(ev);
	}

	public void toggleZoomeScale() {
		if (mLastScale > 1.0f) {
			reset();
			scaleDown();
		} else {
			scaleUp();
		}
	}

	public boolean setZoomScale(float scaleFactor) {
		FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) mZoomContainer.getLayoutParams();
		ViewGroup.LayoutParams headLayoutParams = mHeaderContainer.getLayoutParams();

		if ((mLastScale <= 1.0f) && (scaleFactor < mLastScale)) {
			localLayoutParams.height = mZoomHeight;
			localLayoutParams.width = mZoomWidth;
			localLayoutParams.gravity = Gravity.CENTER;
			headLayoutParams.height = mZoomHeight;
			mZoomContainer.setLayoutParams(localLayoutParams);
			mHeaderContainer.setLayoutParams(headLayoutParams);
			return false;
		}
		mLastScale = Math.min(Math.max(scaleFactor, 1.0f), mMaxScale);
		localLayoutParams.height = ((int) (mZoomHeight * mLastScale));
		// localLayoutParams.width = ((int) (mZoomWidth * mLastScale));
		localLayoutParams.gravity = Gravity.CENTER;
		headLayoutParams.height = ((int) (mZoomHeight * mLastScale));
		if (localLayoutParams.height < mScreenHeight) {
			mZoomContainer.setLayoutParams(localLayoutParams);
			mHeaderContainer.setLayoutParams(headLayoutParams);
		}

		return true;
	}

	private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
		int i = (paramMotionEvent.getAction()) >> 8;
		if (MotionEventCompat.getPointerId(paramMotionEvent, i) == mActivePointerId)
			if (i != 0) {
				mLastMotionY = MotionEventCompat.getY(paramMotionEvent, 0);
				mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
			}
	}

	private void reset() {
		this.mActivePointerId = -1;
		this.mLastMotionY = -1.0F;
		this.mLastScale = -1.0F;
	}

	class ScalingRunnable implements Runnable {
		long mDuration;
		boolean mIsFinished = true;
		float currentScale;
		float mScale;
		long mStartTime;

		float scaleDiff;

		ScalingRunnable() {
		}

		public void abortAnimation() {
			mIsFinished = true;
		}

		public boolean isFinished() {
			return mIsFinished;
		}

		public void run() {
			float f2;
			FrameLayout.LayoutParams localLayoutParams;
			ViewGroup.LayoutParams headLayoutParams;
			if ((!mIsFinished) && (mScale >= 1.0f)) {
				float f1 = ((float) (System.currentTimeMillis() - mStartTime)) / (float) mDuration;
				f2 = currentScale - scaleDiff * sInterpolator.getInterpolation(f1);

				localLayoutParams = (FrameLayout.LayoutParams) mZoomContainer.getLayoutParams();
				headLayoutParams = mHeaderContainer.getLayoutParams();
				if (f1 < 1.0f) {
					localLayoutParams.height = ((int) (f2 * mZoomHeight));
					// localLayoutParams.width = ((int) (f2 * mZoomWidth));
					localLayoutParams.gravity = Gravity.CENTER;
					mZoomContainer.setLayoutParams(localLayoutParams);
					headLayoutParams.height = ((int) (f2 * mZoomHeight));
					mHeaderContainer.setLayoutParams(headLayoutParams);
					post(this);
					return;
				}
				mIsFinished = true;
			}
		}

		public void startAnimation(float scale, long paramLong) {
			mStartTime = System.currentTimeMillis();
			mDuration = paramLong;
			currentScale = ((float) mZoomContainer.getBottom() / mZoomHeight);
			mScale = scale;

			scaleDiff = currentScale - mScale;
			mIsFinished = false;
			post(this);
		}
	}

	public interface OnScrollViewChangedListener {
		public void onScrollChanged(int left, int top, int oldLeft, int oldTop);
	}

	public void setOverScrollEnabled(boolean enabled) {
		mIsOverScrollEnabled = enabled;
	}

	public boolean isOverScrollEnabled() {
		return mIsOverScrollEnabled;
	}

	@Override
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
			int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
				mIsOverScrollEnabled ? maxOverScrollX : 0, mIsOverScrollEnabled ? maxOverScrollY : 0, isTouchEvent);
	}
}