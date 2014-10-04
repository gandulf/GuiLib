package com.gandulf.guilib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gandulf.guilib.R;
import com.gandulf.guilib.view.ViewScroller.ScrollingListener;

public class SeekBarEx extends SeekBar {

	public interface SeekBarLabelRenderer {
		public String render(int value);
	}

	private class OnSeekBarChangeListenerWrapper implements OnSeekBarChangeListener, ScrollingListener {

		private OnSeekBarChangeListener wrapped;
		private ViewScroller scroller;

		private int currentOffsetX = 0;

		public OnSeekBarChangeListenerWrapper(OnSeekBarChangeListener wrapped) {
			this.wrapped = wrapped;
			scroller = new ViewScroller(getContext(), this, LinearLayout.HORIZONTAL);
		}

		public OnSeekBarChangeListener getWrapped() {
			return wrapped;
		}

		public void setWrapped(OnSeekBarChangeListener wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (popupWindow != null && popupWindow.isShowing()) {
				((TextView) popupWindow.getContentView()).setText(String.valueOf(progress + getMin()));
				int offsetX = (int) (seekBar.getWidth() * (seekBar.getProgress() / (float) getMax()));
				offsetX -= popupWindow.getContentView().getWidth() / 2;
				int distance = currentOffsetX - offsetX;

				scroller.scroll(distance, 0);
			}

			if (label != null) {
				if (labelRenderer != null) {
					label.setText(labelRenderer.render(getValue()));
				} else {
					label.setText(Integer.toString(getValue()));
				}
			}
			if (wrapped != null) {
				wrapped.onProgressChanged(seekBar, progress, fromUser);
			}

		}

		private void initPopupwindow() {
			if (popupWindow == null) {
				TextView textView = (TextView) inflate(getContext(), R.layout.popup_text, null);
				popupWindow = new PopupWindow(textView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				popupWindow.setContentView(textView);
				textView.measure(0, 0);
				popupWindow.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.popup_background));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			initPopupwindow();

			currentOffsetX = (int) (seekBar.getWidth() * (seekBar.getProgress() / (float) getMax()));
			currentOffsetX -= popupWindow.getContentView().getMeasuredWidth() / 2;

			popupWindow.showAsDropDown(seekBar, currentOffsetX, 0);

			if (wrapped != null) {
				wrapped.onStartTrackingTouch(seekBar);
			}

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (popupWindow != null) {
				popupWindow.dismiss();
			}

			if (label != null) {
				if (labelRenderer != null) {
					label.setText(labelRenderer.render(getValue()));
				} else {
					label.setText(Integer.toString(getValue()));
				}
			}

			if (wrapped != null) {
				wrapped.onStopTrackingTouch(seekBar);
			}
		}

		@Override
		public void onFinished() {

		}

		@Override
		public void onJustify() {

		}

		@Override
		public void onScroll(int distance) {
			if (popupWindow != null) {
				currentOffsetX += distance;
				popupWindow.update(SeekBarEx.this, currentOffsetX, 0, -1, -1);
			}
		}

		@Override
		public void onStarted() {

		}

	}

	private PopupWindow popupWindow;

	private OnSeekBarChangeListenerWrapper wrapper;

	private SeekBarLabelRenderer labelRenderer;

	private int min = 0;
	private int stepSize = 1;

	private TextView label;

	public SeekBarEx(Context context) {
		super(context);
		init();
	}

	public SeekBarEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SeekBarEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		int diff = this.min - min;

		super.setMax(getMax() + diff);
		super.setProgress(getProgress() + diff);
		this.min = min;
		invalidate();
	}

	@Override
	public synchronized void setMax(int max) {
		super.setMax(max - getMin());
		invalidate();
	}

	public int getValue() {
		return getProgress() + min;
	}

	public void setValue(int v) {
		setProgress(v - min);
	}

	public TextView getLabel() {
		return label;
	}

	public void setLabel(TextView label) {
		this.label = label;
	}

	private void init() {
		wrapper = new OnSeekBarChangeListenerWrapper(null);
		super.setOnSeekBarChangeListener(wrapper);
	}

	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		wrapper.setWrapped(l);
	}

	public SeekBarLabelRenderer getLabelRenderer() {
		return labelRenderer;
	}

	public void setLabelRenderer(SeekBarLabelRenderer labelRenderer) {
		this.labelRenderer = labelRenderer;
	}

}
