package fr.castorflex.android.flipimageview.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.gandulf.guilib.R;
import com.gandulf.guilib.listener.CheckableListenable;
import com.gandulf.guilib.listener.OnCheckedChangeListener;
import com.gandulf.guilib.util.ResUtil;

/**
 * Created with IntelliJ IDEA. User: castorflex Date: 30/12/12 Time: 16:25
 */
public class FlipImageView extends ImageView implements View.OnClickListener, Animation.AnimationListener {

	private static final int FLAG_ROTATION_X = 1 << 0;

	private static final int FLAG_ROTATION_Y = 1 << 1;

	private static final int FLAG_ROTATION_Z = 1 << 2;

	private static final Interpolator fDefaultInterpolator = new DecelerateInterpolator();

	public interface OnFlipListener {

		public void onClick(FlipImageView view);

		public void onFlipStart(FlipImageView view);

		public void onFlipEnd(FlipImageView view);
	}

	private OnFlipListener mListener;

	private boolean mIsFlipped;

	private boolean mIsDefaultAnimated;

	private Drawable mDrawable;
	private Drawable mBackground;

	private Drawable mFlippedDrawable;
	private Drawable mFlippedBackground;

	private FlipAnimator mAnimation;

	private boolean mIsRotationXEnabled;

	private boolean mIsRotationYEnabled;

	private boolean mIsRotationZEnabled;

	private boolean mIsFlipping;

	private boolean mIsRotationReversed;

	private boolean mIsFlippable = true;

	public FlipImageView(Context context) {
		super(context);
		init(context, null, R.attr.flipImageViewStyle);
	}

	public FlipImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, R.attr.flipImageViewStyle);
	}

	public FlipImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		TypedArray a = context
				.obtainStyledAttributes(attrs, R.styleable.FlipImageView, defStyle, R.style.FlipImageView);

		mIsDefaultAnimated = a.getBoolean(R.styleable.FlipImageView_isAnimated, true);
		mIsFlipped = a.getBoolean(R.styleable.FlipImageView_isFlipped, false);
		mFlippedDrawable = a.getDrawable(R.styleable.FlipImageView_flipDrawable);
		int duration = a.getInt(R.styleable.FlipImageView_flipDuration, 500);
		int interpolatorResId = a.getResourceId(R.styleable.FlipImageView_flipInterpolator, 0);
		Interpolator interpolator = interpolatorResId > 0 ? AnimationUtils.loadInterpolator(context, interpolatorResId)
				: fDefaultInterpolator;

		int rotations = a.getInteger(R.styleable.FlipImageView_flipRotations, FLAG_ROTATION_Y);
		mIsRotationXEnabled = (rotations & FLAG_ROTATION_X) != 0;
		mIsRotationYEnabled = (rotations & FLAG_ROTATION_Y) != 0;
		mIsRotationZEnabled = (rotations & FLAG_ROTATION_Z) != 0;

		mDrawable = getDrawable();
		mBackground = getBackground();
		mIsRotationReversed = a.getBoolean(R.styleable.FlipImageView_reverseRotation, false);

		mAnimation = new FlipAnimator();
		mAnimation.setAnimationListener(this);
		mAnimation.setInterpolator(interpolator);
		mAnimation.setDuration(duration);

		setOnClickListener(this);

		super.setImageDrawable(mIsFlipped ? mFlippedDrawable : mDrawable);
		super.setBackgroundDrawable(mIsFlipped ? mFlippedBackground : mBackground);
		mIsFlipping = false;

		a.recycle();
	}

	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		mBackground = background;
		if (!mIsFlippable || !mIsFlipped) {
			super.setBackgroundDrawable(background);
		}
	}

	public void setFlippedDrawable(Drawable flippedDrawable) {
		mFlippedDrawable = flippedDrawable;
		if (mIsFlipped)
			super.setImageDrawable(mFlippedDrawable);
	}

	public void setImageDrawable(Drawable drawable) {
		mDrawable = drawable;
		if (!mIsFlippable || !mIsFlipped)
			super.setImageDrawable(mDrawable);
	}

	@Override
	public void setImageResource(int resId) {
		if (resId != 0) {
			mDrawable = getContext().getResources().getDrawable(resId);
		} else {
			mDrawable = null;
		}
		if (!mIsFlippable || !mIsFlipped)
			super.setImageResource(resId);
	}

	@Override
	public void setImageURI(Uri uri) {
		if (uri != null) {
			mDrawable = ResUtil.getDrawableByUri(getContext(), uri);
		} else {
			mDrawable = null;
		}
		if (!mIsFlippable || !mIsFlipped) {
			super.setImageURI(uri);
		}
	}

	public boolean isRotationXEnabled() {
		return mIsRotationXEnabled;
	}

	public void setRotationXEnabled(boolean enabled) {
		mIsRotationXEnabled = enabled;
	}

	public boolean isRotationYEnabled() {
		return mIsRotationYEnabled;
	}

	public void setRotationYEnabled(boolean enabled) {
		mIsRotationYEnabled = enabled;
	}

	public boolean isRotationZEnabled() {
		return mIsRotationZEnabled;
	}

	public void setRotationZEnabled(boolean enabled) {
		mIsRotationZEnabled = enabled;
	}

	public FlipAnimator getFlipAnimation() {
		return mAnimation;
	}

	public void setInterpolator(Interpolator interpolator) {
		mAnimation.setInterpolator(interpolator);
	}

	public void setDuration(int duration) {
		mAnimation.setDuration(duration);
	}

	public boolean isFlipped() {
		return mIsFlipped;
	}

	public boolean isFlipping() {
		return mIsFlipping;
	}

	public boolean isFlippable() {
		return mIsFlippable;
	}

	public void setFlippable(boolean v) {
		if (!v) {
			setFlipped(false, false);
		}
		mIsFlippable = v;
	}

	public boolean isRotationReversed() {
		return mIsRotationReversed;
	}

	public void setRotationReversed(boolean rotationReversed) {
		mIsRotationReversed = rotationReversed;
	}

	public boolean isAnimated() {
		return mIsDefaultAnimated;
	}

	public void setAnimated(boolean animated) {
		mIsDefaultAnimated = animated;
	}

	public void setFlipped(boolean flipped) {
		if (mIsFlippable)
			setFlipped(flipped, mIsDefaultAnimated);
	}

	public void setFlipped(boolean flipped, boolean animated) {

		if (mIsFlippable && flipped != mIsFlipped) {
			toggleFlip(animated);
		}
	}

	public void toggleFlip() {
		toggleFlip(mIsDefaultAnimated);
	}

	public void toggleFlip(boolean animated) {
		if (mIsFlippable) {
			if (animated) {
				mAnimation.setToBackground(mIsFlipped ? mBackground : mFlippedBackground);
				mAnimation.setToDrawable(mIsFlipped ? mDrawable : mFlippedDrawable);
				startAnimation(mAnimation);
			} else {
				super.setBackgroundDrawable(mIsFlipped ? mBackground : mFlippedBackground);
				super.setImageDrawable(mIsFlipped ? mDrawable : mFlippedDrawable);
			}
			mIsFlipped = !mIsFlipped;
		}
	}

	public void setOnFlipListener(OnFlipListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		toggleFlip();
		if (mIsFlippable && mListener != null) {
			mListener.onClick(this);
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if (mListener != null) {
			mListener.onFlipStart(this);
		}
		mIsFlipping = true;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mListener != null) {
			mListener.onFlipEnd(this);
		}
		mIsFlipping = false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	/**
	 * Animation part All credits goes to coomar
	 */
	public class FlipAnimator extends Animation {

		private Camera camera;

		private Drawable toDrawable;
		private Drawable toBackground;

		private float centerX;

		private float centerY;

		private boolean visibilitySwapped;

		public void setToDrawable(Drawable to) {
			toDrawable = to;
			visibilitySwapped = false;
		}

		public void setToBackground(Drawable to) {
			toBackground = to;
			visibilitySwapped = false;
		}

		public FlipAnimator() {
			setFillAfter(true);
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			camera = new Camera();
			this.centerX = width / 2;
			this.centerY = height / 2;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			// Angle around the y-axis of the rotation at the given time. It is
			// calculated both in radians and in the equivalent degrees.
			final double radians = Math.PI * interpolatedTime;
			float degrees = (float) (180.0 * radians / Math.PI);

			if (mIsRotationReversed) {
				degrees = -degrees;
			}

			// Once we reach the midpoint in the animation, we need to hide the
			// source view and show the destination view. We also need to change
			// the angle by 180 degrees so that the destination does not come in
			// flipped around. This is the main problem with SDK sample, it does not
			// do this.
			if (interpolatedTime >= 0.5f) {
				if (mIsRotationReversed) {
					degrees += 180.f;
				} else {
					degrees -= 180.f;
				}

				if (!visibilitySwapped) {
					FlipImageView.super.setBackgroundDrawable(toBackground);
					FlipImageView.super.setImageDrawable(toDrawable);
					visibilitySwapped = true;
				}
			}

			final Matrix matrix = t.getMatrix();

			camera.save();
			camera.translate(0.0f, 0.0f, (float) (150.0 * Math.sin(radians)));
			camera.rotateX(mIsRotationXEnabled ? degrees : 0);
			camera.rotateY(mIsRotationYEnabled ? degrees : 0);
			camera.rotateZ(mIsRotationZEnabled ? degrees : 0);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

	private static OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(View checkableView, boolean isChecked) {
			if (checkableView.getTag() instanceof FlippableViewHolder) {
				FlippableViewHolder holder = (FlippableViewHolder) checkableView.getTag();
				if (holder.flip != null) {
					holder.flip.setRotationReversed(isChecked);
					holder.flip.setFlipped(isChecked, true);
				}
			}
		}

	};

	private static OnClickListener flipClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			FlipImageView flipImageView = (FlipImageView) v;
			if (v.getTag() instanceof FlippableViewHolder) {
				FlippableViewHolder listHolder = (FlippableViewHolder) v.getTag();
				listHolder.list.setItemChecked(listHolder.position, !flipImageView.isFlipped());
			}
		}
	};

	/**
	 * Helper class for Adapters to extends ViewHolder if flipping in listviews is needed
	 * 
	 */
	public static class FlippableViewHolder {
		public FlipImageView flip;

		public ListView list;
		public int position;

		public static void prepare(int position, View convertView, ViewGroup parent) {

			if (convertView.getTag() instanceof FlippableViewHolder) {
				FlippableViewHolder checkableViewHolder = (FlippableViewHolder) convertView.getTag();

				if (parent instanceof ListView) {
					checkableViewHolder.list = (ListView) parent;
					checkableViewHolder.position = position;
					if (checkableViewHolder.flip != null) {
						checkableViewHolder.flip.setOnClickListener(flipClickListener);
						checkableViewHolder.flip.setTag(checkableViewHolder);
					}
				} else {
					checkableViewHolder.list = null;
					checkableViewHolder.position = AdapterView.INVALID_POSITION;
					if (checkableViewHolder.flip != null) {
						checkableViewHolder.flip.setOnClickListener(null);
						checkableViewHolder.flip.setTag(null);
					}
				}

				if (convertView instanceof CheckableListenable) {
					CheckableListenable checkable = (CheckableListenable) convertView;
					if (checkableViewHolder.flip != null) {
						checkableViewHolder.flip.setFlipped(checkable.isChecked(), false);
						checkableViewHolder.flip.setRotationReversed(checkable.isChecked());
					}
					checkable.setOnCheckedChangeListener(checkedChangeListener);
				}
			}

		}
	}
}
