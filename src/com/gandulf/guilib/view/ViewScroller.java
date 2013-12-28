package com.gandulf.guilib.view;

import kankan.wheel.widget.WheelView;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates the
 */
public class ViewScroller {
	/**
	 * Scrolling listener interface
	 */
	public interface ScrollingListener {
		/**
		 * Scrolling callback called when scrolling is performed.
		 * 
		 * @param distance
		 *            the distance to scroll
		 */
		void onScroll(int distance);

		/**
		 * Starting callback called when scrolling is started
		 */
		void onStarted();

		/**
		 * Finishing callback called after justifying
		 */
		void onFinished();

		/**
		 * Justifying callback called to justify a view when scrolling is ended
		 */
		void onJustify();
	}

	/** Scrolling duration */
	private static final int SCROLLING_DURATION = 400;

	/** Minimum delta for scrolling */
	public static final int MIN_DELTA_FOR_SCROLLING = 1;

	// Listener
	private ScrollingListener listener;

	// Context
	private Context context;

	// Scrolling

	private Scroller scroller;
	private int lastScrollY;
	private boolean isScrollingPerformed;
	private int orientation;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param listener
	 *            the scrolling listener
	 */
	public ViewScroller(Context context, ScrollingListener listener, int orientation) {

		this.orientation = orientation;
		this.scroller = new Scroller(context);
		this.listener = listener;
		this.context = context;
	}

	/**
	 * Set the the specified scrolling interpolator
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.forceFinished(true);
		scroller = new Scroller(context, interpolator);
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	/**
	 * Scroll the wheel
	 * 
	 * @param distance
	 *            the scrolling distance
	 * @param time
	 *            the scrolling duration
	 */
	public void scroll(int distance, int time) {
		scroller.forceFinished(true);

		lastScrollY = 0;

		if (orientation == WheelView.VERTICAL) {
			scroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
		} else {
			scroller.startScroll(0, 0, distance, 0, time != 0 ? time : SCROLLING_DURATION);
		}
		setNextMessage(MESSAGE_SCROLL);

		startScrolling();
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.forceFinished(true);
	}

	// Messages
	private final int MESSAGE_SCROLL = 0;
	private final int MESSAGE_JUSTIFY = 1;

	/**
	 * Set next message to queue. Clears queue before.
	 * 
	 * @param message
	 *            the message to set
	 */
	private void setNextMessage(int message) {
		clearMessages();
		animationHandler.sendEmptyMessage(message);
	}

	/**
	 * Clears messages from queue
	 */
	private void clearMessages() {
		animationHandler.removeMessages(MESSAGE_SCROLL);
		animationHandler.removeMessages(MESSAGE_JUSTIFY);
	}

	// animation handler
	private Handler animationHandler = new Handler() {
		public void handleMessage(Message msg) {
			scroller.computeScrollOffset();
			int currY, finalY, delta;
			if (orientation == WheelView.VERTICAL) {
				currY = scroller.getCurrY();
				finalY = scroller.getFinalY();
			} else {
				currY = scroller.getCurrX();
				finalY = scroller.getFinalX();
			}

			delta = lastScrollY - currY;
			lastScrollY = currY;
			if (delta != 0) {
				listener.onScroll(delta);
			}

			// scrolling is not finished when it comes to final Y
			// so, finish it manually
			if (Math.abs(currY - finalY) < MIN_DELTA_FOR_SCROLLING) {
				currY = finalY;
				scroller.forceFinished(true);
			}
			if (!scroller.isFinished()) {
				animationHandler.sendEmptyMessage(msg.what);
			} else if (msg.what == MESSAGE_SCROLL) {
				justify();
			} else {
				finishScrolling();
			}
		}
	};

	/**
	 * Justifies wheel
	 */
	private void justify() {
		listener.onJustify();
		setNextMessage(MESSAGE_JUSTIFY);
	}

	/**
	 * Starts scrolling
	 */
	private void startScrolling() {
		if (!isScrollingPerformed) {
			isScrollingPerformed = true;
			listener.onStarted();
		}
	}

	/**
	 * Finishes scrolling
	 */
	void finishScrolling() {
		if (isScrollingPerformed) {
			listener.onFinished();
			isScrollingPerformed = false;
		}
	}

}
