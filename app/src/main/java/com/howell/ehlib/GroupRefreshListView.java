package com.howell.ehlib;
/**
 * @author 霍之昊 
 *
 * 类说明
 */

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.howell.ecamcompany.R;

import java.util.Date;


public class GroupRefreshListView extends ListView {

	private static final String TAG = "listview";

	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;


	private final static int RATIO = 3;

	private LayoutInflater inflater;

	private LinearLayout headView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;


	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;


	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	private OnRefreshListener refreshListener;

	private boolean isRefreshable;

	public GroupRefreshListView(Context context) {
		super(context);
		init(context);
	}

	public GroupRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.head, null);

		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
//		arrowImageView.setMinimumWidth(70);
//		arrowImageView.setMinimumHeight(50);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		Log.v("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);

		addHeaderView(headView, null, false);
//		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		isRefreshable = false;
		state = REFRESHING;
		changeHeaderViewByState();
		
		new AsyncTask<Void, Integer, Void>() {


			@SuppressWarnings("WrongThread")
			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				try {
					onFirstRefresh();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				onFirstRefreshDown();
				super.onPostExecute(result);
			}
			
		}.execute();
		
	}
	
	

//	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2,
//			int arg3) {
//		firstItemIndex = firstVisiableItem;
//	}
//
//	public void onScrollStateChanged(AbsListView arg0, int arg1) {
//	}

	public void setFirstItemIndex(int firstItemIndex) {
		this.firstItemIndex = firstItemIndex;
	}

	public boolean onTouchEvent(MotionEvent event) {

		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					Log.v(TAG, "action down");
				}
				break;

			case MotionEvent.ACTION_UP:

				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {

					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();
						Log.v(TAG,"PULL_To_REFRESH");

					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();
						Log.v(TAG,"RELEASE_To_REFRESH");

					}
				}

				isRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					Log.v(TAG,"ACTION_MOVE");
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored && state != LOADING) {


					if (state == RELEASE_To_REFRESH) {

						setSelection(0);


						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
							Log.v(TAG,"PULL_To_REFRESH");

						}

						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG,"tempY - startY <= 0");
						}

						else {

						}
					}

					if (state == PULL_To_REFRESH) {

						setSelection(0);


						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();
							Log.v(TAG,"RELEASE_To_REFRESH");

						}

						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();
							Log.v(TAG,"tempY - startY <= 0");

						}
					}


					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}


					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}


					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}

				}

				break;
			}
		}

		return super.onTouchEvent(event);
	}

	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText(getResources().getString(R.string.release_to_refresh));

			Log.v(TAG,"RELEASE_To_REFRESH");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);

			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText(getResources().getString(R.string.pull_to_refresh));
			} else {
				tipsTextview.setText(getResources().getString(R.string.pull_to_refresh));
			}
			Log.v(TAG,"PULL_To_REFRESH");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText(getResources().getString(R.string.refreshing));
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG,"REFRESHING");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.mipmap.z_arrow_down);
			tipsTextview.setText(getResources().getString(R.string.pull_to_refresh));
			lastUpdatedTextView.setVisibility(View.VISIBLE);


			break;
		}
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
		public void onFirstRefresh();
		public void onFirstRefreshDown();
	}

	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText(getResources().getString(R.string.last_update) + ":" + new Date().toLocaleString());
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}
	
	private void onFirstRefresh() throws InterruptedException {
		while (refreshListener == null) {
			System.out.println("123 wait onFirstRefresh sleep");
			//Thread.sleep(100);
			return;
		}
		refreshListener.onFirstRefresh();
	}
	
	private void onFirstRefreshDown() {
		if (refreshListener != null) {
			refreshListener.onFirstRefreshDown();
		}
	}


	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		lastUpdatedTextView.setText(getResources().getString(R.string.last_update) + ":" + new Date().toLocaleString());
		super.setAdapter(adapter);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		//Log.e("", "dispatchDraw");
	    try {
	        super.dispatchDraw(canvas);
	    } catch (IndexOutOfBoundsException e) {
	            // samsung error
	    	Log.e("123", "samsung error");
	    } catch (StackOverflowError e){
	    	Log.e("StackOverflowError", "error");
	    }
	}

}

