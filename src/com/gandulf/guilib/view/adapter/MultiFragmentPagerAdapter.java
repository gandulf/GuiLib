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
package com.gandulf.guilib.view.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public abstract class MultiFragmentPagerAdapter extends PagerAdapter {

	protected List<Fragment> mCurrentPrimaryItems = new ArrayList<Fragment>();

	protected FragmentManager mFragmentManager;
	protected FragmentTransaction mCurTransaction;

	protected Context mContext;

	public MultiFragmentPagerAdapter(Context context, FragmentManager fm) {
		super();
		this.mContext = context;
		this.mFragmentManager = fm;
	}

	public abstract Fragment getItem(int position, int tab);

	public abstract int getCount();

	public abstract int getCount(int position);

	@Override
	public Object instantiateItem(View container, int position) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		LinearLayout v = new LinearLayout(mContext);
		v.setOrientation(LinearLayout.HORIZONTAL);
		v.setWeightSum(getCount(position));

		int startIndex = getStartIndex(position);

		int containerId = 0;
		final int size = getCount(position);
		for (int i = 0; i < size; i++) {

			containerId = startIndex + i;

			String name = makeFragmentName(container.getId(), containerId);
			Fragment fragment = mFragmentManager.findFragmentByTag(name);

			FrameLayout fragmentView = new FrameLayout(mContext);
			LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
			fragmentView.setLayoutParams(layoutParams);
			fragmentView.setId(containerId);
			v.addView(fragmentView, i);

			if (fragment != null) {
				Debug.verbose("Attaching item #" + position + ": f=" + fragment + " id:" + containerId);
				mCurTransaction.attach(fragment);
			} else {
				// index is 1 based remove 1 to get a 0-based
				fragment = getItem(position, i);
				if (fragment != null) {
					Debug.verbose("Adding item #" + position + ": f=" + fragment + " id:" + containerId);
					mCurTransaction.add(containerId, fragment, name);
				}
			}
			if (fragment != null && !mCurrentPrimaryItems.contains(fragment)) {
				fragment.setMenuVisibility(false);
				fragment.setUserVisibleHint(false);
			}
		}

		mCurTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		((ViewPager) container).addView(v, 0);

		return v;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		int containerId = 0;
		final int startIndex = getStartIndex(position);
		final int size = getCount(position);
		for (int i = 0; i < size; i++) {
			containerId = startIndex + i;
			String name = makeFragmentName(container.getId(), containerId);
			Fragment fragment = mFragmentManager.findFragmentByTag(name);
			if (fragment != null) {
				Debug.verbose("Detaching item #" + position + ": f=" + object + " v=" + fragment.getView());
				mCurTransaction.detach(fragment);
			}
		}

		((ViewPager) container).removeView((LinearLayout) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((LinearLayout) object);
	}

	@Override
	public void setPrimaryItem(View container, int position, Object object) {

		List<Fragment> newPrimaryItems = new ArrayList<Fragment>(getCount(position));
		int containerId = 0;
		final int startIndex = getStartIndex(position);
		final int size = getCount(position);
		for (int i = 0; i < size; i++) {

			containerId = startIndex + i;
			String name = makeFragmentName(container.getId(), containerId);
			Fragment fragment = mFragmentManager.findFragmentByTag(name);
			if (fragment != null) {
				newPrimaryItems.add(fragment);
			}

		}

		mCurrentPrimaryItems.removeAll(newPrimaryItems);
		for (Fragment frag : mCurrentPrimaryItems) {
			frag.setMenuVisibility(false);
			frag.setUserVisibleHint(false);
		}

		for (Fragment frag : newPrimaryItems) {
			frag.setMenuVisibility(true);
			frag.setUserVisibleHint(true);
		}

		mCurrentPrimaryItems = newPrimaryItems;

	}

	@Override
	public void finishUpdate(View container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	protected String makeFragmentName(int viewId, int position) {
		return "android:switcher:" + viewId + ":" + position;
	}

	protected int getStartIndex(int position) {
		int index = 1;
		for (int i = 0; i < position; i++) {
			index += getCount(i);
		}
		return index;

	}

}
