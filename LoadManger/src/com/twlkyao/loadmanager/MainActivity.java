package com.twlkyao.loadmanager;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * 使用加载器加载通话记录
 * Use LoadManager to load call logs.
 * @author Administrator
 * @editor Shiyao Qi
 */
public class MainActivity extends Activity {

	private static final String TAG = "dzt";
	
	// 查询指定的条目
	// query specified items.
	// id, telephone number, cached name, call types, call date.
	private static final String[] CALLLOG_PROJECTION = new String[] {
			CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
			CallLog.Calls.TYPE, CallLog.Calls.DATE };
	
	static final int DAY = 1440; // 一天的分钟值 minutes of a day.
	private static final int ALL = 0; // 默认显示所有
	private static final int INCOMING = CallLog.Calls.INCOMING_TYPE; // 来电 incomming call.
	private static final int OUTCOMING = CallLog.Calls.OUTGOING_TYPE; // 拔号 outcomming call.
	private static final int MISSED = CallLog.Calls.MISSED_TYPE; // 未接 missed cal.
	
	private ListView mListView;
	private MyLoaderListener mLoaderListener = new MyLoaderListener();
	private MyCursorAdapter mAdapter;
	private int mCallLogShowType = ALL;
	private boolean m_FinishLoaderFlag = false; // 第一次加载完成 flag to indicate whether the load is finished.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initWidgets();
		initMyLoader();
	}

	/**
	 * init the widgets and set their listeners and adapters.
	 */
	private void initWidgets() {
		mListView = (ListView) findViewById(R.id.lv_list); // ListView to hold the call logs.
	
		// show all call logs button.
		Button btn = (Button) findViewById(R.id.btn_all);
		btn.setOnClickListener(new buttonListener());
		
		// show incomming call logs button.
		btn = (Button) findViewById(R.id.btn_incoming);
		btn.setOnClickListener(new buttonListener());
		
		// show outcomming call logs button.
		btn = (Button) findViewById(R.id.btn_outcoming);
		btn.setOnClickListener(new buttonListener());
		
		// show missed call logs button.
		btn = (Button) findViewById(R.id.btn_missed);
		btn.setOnClickListener(new buttonListener());
		
		mAdapter = new MyCursorAdapter(MainActivity.this, null); // get an adapter.
		mListView.setAdapter(mAdapter); // set adapter for the ListView.
	}

	private void initMyLoader() {
		getLoaderManager().initLoader(0, null, mLoaderListener);
	}

	/**
	 * 实现一个加载器
	 * @author Administrator
	 */
	private class MyLoaderListener implements
			LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			m_FinishLoaderFlag = false; // flag to indicate that the load is not finished.
			CursorLoader cursor = new CursorLoader(MainActivity.this,
					CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION, null, null,
					CallLog.Calls.DEFAULT_SORT_ORDER);
			Log.d(TAG, "MyLoaderListener---------->onCreateLoader");
			return cursor;
		}

		/**
		 * load data according to different conditions.
		 */
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			// TODO Auto-generated method stub
			if (data == null)
				return;
			Cursor tempData = data;
			if (tempData.getCount() == 0) { // there is no data.
				Log.d(TAG,
						"MyLoaderListener---------->onLoadFinished count = 0");
				mAdapter.swapCursor(null);
				return;
			}
			if (m_FinishLoaderFlag) { // if the data is all loaded.
				tempData = null;
				String selection = null;
				String[] selectionArgs = null;
				if (mCallLogShowType == INCOMING) { // incomming calls.
					selection = CallLog.Calls.TYPE + "=?";
					selectionArgs = new String[] { "1" };
				} else if (mCallLogShowType == OUTCOMING) { // outcomming calls.
					selection = CallLog.Calls.TYPE + "=?";
					selectionArgs = new String[] { "2" };
				} else if (mCallLogShowType == MISSED) { // missed calls.
					selection = CallLog.Calls.TYPE + "=?";
					selectionArgs = new String[] { "3" };
				}
				
				// get the selected data's cursor.
				tempData = getContentResolver().query(
						CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION,
						selection, selectionArgs,
						CallLog.Calls.DEFAULT_SORT_ORDER);
			}
			mAdapter.swapCursor(tempData); // swap to the new cursor.
			Log.d(TAG,
					"MyLoaderListener---------->onLoadFinished data count = "
							+ data.getCount());
			m_FinishLoaderFlag = true; // set the flag to indicate that all the data is loaded.
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			Log.d(TAG, "MyLoaderListener---------->onLoaderReset");
			mAdapter.swapCursor(null);
		}
	}

	/**
	 * Set different listener according to the type of the button.
	 * @author Jack
	 *
	 */
	private class buttonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_all:
				allCalllog();
				break;
			case R.id.btn_incoming:
				incomingCalllog();
				break;
			case R.id.btn_outcoming:
				outcomingCalllog();
				break;
			case R.id.btn_missed:
				missedCalllog();
				break;
			default:
				break;
			} // switch.
		} // onClick.
	} // buttonListener.

	/**
	 * show all call logs.
	 */
	private void allCalllog() {
		mCallLogShowType = ALL; // 0.
		String selection = null;
		String[] selectionArgs = null;
		Cursor allCursor = getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION, selection,
				selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
		mAdapter.swapCursor(allCursor);
	}

	/**
	 * show incomming call logs.
	 */
	private void incomingCalllog() {
		mCallLogShowType = INCOMING;
		String selection = CallLog.Calls.TYPE + "=?";
		String[] selectionArgs = new String[] { "1" };
		Cursor incomingCursor = getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION, selection,
				selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
		mAdapter.swapCursor(incomingCursor);
	}

	/**
	 * show outcommign call logs.
	 */
	private void outcomingCalllog() {
		mCallLogShowType = OUTCOMING;
		String selection = CallLog.Calls.TYPE + "=?";
		String[] selectionArgs = new String[] { "2" };
		Cursor outcomingCursor = getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION, selection,
				selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
		mAdapter.swapCursor(outcomingCursor);
	}

	/**
	 * show miss call logs.
	 */
	private void missedCalllog() {
		mCallLogShowType = MISSED;
		String selection = CallLog.Calls.TYPE + "=?";
		String[] selectionArgs = new String[] { "3" };
		Cursor missedCursor = getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALLLOG_PROJECTION, selection,
				selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
		mAdapter.swapCursor(missedCursor);
	}
}
