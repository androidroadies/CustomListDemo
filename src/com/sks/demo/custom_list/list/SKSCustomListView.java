package com.sks.demo.custom_list.list;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sks.demo.custom_list.list.SKSCustomListAdapter.HasMorePagesListener;

/**
 * A ListView that maintains a header pinned at the top of the list. The pinned
 * header can be pushed up and dissolved as needed.
 * 
 * It also supports pagination by setting a custom view as the loading
 * indicator.
 */
public class SKSCustomListView extends ListView implements HasMorePagesListener {

	View listFooter;
	boolean footerViewAttached = false;

	private View mHeaderView;
	private boolean mHeaderViewVisible;

	private int mHeaderViewWidth;
	private int mHeaderViewHeight;

	private SKSCustomListAdapter adapter;

	public void setPinnedHeaderView(View view) {
		mHeaderView = view;

		// Disable vertical fading when the pinned header is present
		// TODO change ListView to allow separate measures for top and bottom
		// fading edge;
		// in this particular case we would like to disable the top, but not the
		// bottom edge.
		if (mHeaderView != null) {
			setFadingEdgeLength(0);
		}
		
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeaderView != null) {
			measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
			mHeaderViewWidth = mHeaderView.getMeasuredWidth();
			mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mHeaderView != null) {
			mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			configureHeaderView(getFirstVisiblePosition());
		}
	}

	public void configureHeaderView(int position) {
		if (mHeaderView == null) {
			return;
		}

		int state = adapter.getPinnedHeaderState(position);
		switch (state) {
		case SKSCustomListAdapter.PINNED_HEADER_GONE: {
			mHeaderViewVisible = false;
			break;
		}

		case SKSCustomListAdapter.PINNED_HEADER_VISIBLE: {
			adapter.configurePinnedHeader(mHeaderView, position, 255);
			if (mHeaderView.getTop() != 0) {
				mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			}
			mHeaderViewVisible = true;
			break;
		}

		case SKSCustomListAdapter.PINNED_HEADER_PUSHED_UP: {
			View firstView = getChildAt(0);
			if (firstView != null) {
				int bottom = firstView.getBottom();
				int headerHeight = mHeaderView.getHeight();
				int y;
				int alpha;
				if (bottom < headerHeight) {
					y = (bottom - headerHeight);
					alpha = 255 * (headerHeight + y) / headerHeight;
				} else {
					y = 0;
					alpha = 255;
				}
				adapter.configurePinnedHeader(mHeaderView, position, alpha);
				if (mHeaderView.getTop() != y) {
					mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
				}
				mHeaderViewVisible = true;
			}
			break;
		}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeaderViewVisible) {
			drawChild(canvas, mHeaderView, getDrawingTime());
		}
		
	}

	public SKSCustomListView(Context context) {
		super(context);
	}

	public SKSCustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SKSCustomListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setLoadingView(View listFooter) {
		this.listFooter = listFooter;
	}

	public View getLoadingView() {
		return listFooter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof SKSCustomListAdapter)) {
			throw new IllegalArgumentException(SKSCustomListView.class.getSimpleName() + " must use adapter of type "
					+ SKSCustomListAdapter.class.getSimpleName());
		}

		// previous adapter
		if (this.adapter != null) {
			this.adapter.setHasMorePagesListener(null);
			this.setOnScrollListener(null);
		}

		this.adapter = (SKSCustomListAdapter) adapter;
		((SKSCustomListAdapter) adapter).setHasMorePagesListener(this);
		this.setOnScrollListener((SKSCustomListAdapter) adapter);

		View dummy = new View(getContext());
		super.addFooterView(dummy);
		super.setAdapter(adapter);
		super.removeFooterView(dummy);
	}

	@Override
	public SKSCustomListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void noMorePages() {
		if (listFooter != null) {
			this.removeFooterView(listFooter);
		}
		footerViewAttached = false;
	}

	@Override
	public void mayHaveMorePages() {
		if (!footerViewAttached && listFooter != null) {
			this.addFooterView(listFooter);
			footerViewAttached = true;
		}
	}

	public boolean isLoadingViewVisible() {
		return footerViewAttached;
	}
}
