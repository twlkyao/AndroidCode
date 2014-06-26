package com.twlkyao.loadmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

class MyCursorAdapter extends CursorAdapter {

	private static final String TAG = "dzt";
	private final Context mContext; // the context which the adapter is in.

	public MyCursorAdapter(Context context, Cursor c) {
		this(context, c, true);
		// TODO Auto-generated constructor stub
	}

	public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public MyCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context); // obtain the layout inflater from given context.
		return inflater.inflate(R.layout.listview_item, parent, false); // get the view of the layout.
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		if (cursor == null)
			return;
		final String id = cursor.getString(0); // the id string of the call log.
		String number = cursor.getString(1); // the call number.
		String name = cursor.getString(2); // the call contact name.
		int type = cursor.getInt(3); // the call type.
		String date = cursor.getString(4); // the call date.
		
		ImageView TypeView = (ImageView) view.findViewById(R.id.bt_icon); // call log type.
		
		// set name for name TextView.
		TextView nameCtrl = (TextView) view.findViewById(R.id.tv_name); // call name.
		if (name == null) {
			nameCtrl.setText(mContext.getString(R.string.name_unknown));
		} else {
			nameCtrl.setText(name);
		}
		
		// set number for number TextView.
		TextView numberCtrl = (TextView) view.findViewById(R.id.tv_number);
		numberCtrl.setText(number);
		
		// set date for date TextView.
		String value = ComputeDate(date); // get the formatted date string.
		TextView dateCtrl = (TextView) view.findViewById(R.id.tv_date);
		dateCtrl.setText(value);
		
		switch (type) {
		case CallLog.Calls.INCOMING_TYPE: // 1
			TypeView.setImageResource(R.drawable.calllog_incoming);
			break;
		case CallLog.Calls.OUTGOING_TYPE: // 2
			TypeView.setImageResource(R.drawable.calllog_outcoming);
			break;
		case CallLog.Calls.MISSED_TYPE: // 3
			TypeView.setImageResource(R.drawable.calllog_missed);
			break;
		case 4: // CallLog.Calls.VOICEMAIL_TYPE

			break;
		default:
			break;
		}

		ImageButton dailBtn = (ImageButton) view.findViewById(R.id.btn_call);
		dailBtn.setTag(number); // set tag with the telephone number.
		dailBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent.ACTION_CALL_PRIVILEGED 由于Intent中隐藏了，只能用字符串代替
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts(
						"tel", (String) v.getTag(), null));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		});

		ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.btn_delete);
		deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 根据ID进行记录删除
				// delete records according to the id.
				String where = CallLog.Calls._ID + "=?";
				String[] selectionArgs = new String[] { id };
				
				// CallLog.Calls.CONTENT_URI is the database of callogs,
				// equals to Uri.parse("content://call_log/calls").
				int result = mContext.getContentResolver().delete(
						CallLog.Calls.CONTENT_URI, where, selectionArgs);
				Log.d(TAG, "11result = " + result);
			}
		});
		
		ImageButton smsButton = (ImageButton) view.findViewById(R.id.btn_sms);
		smsButton.setTag(number); // set tag with the telephone number.
		smsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts(
						"smsto", (String) v.getTag(), null));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent); 
			}
		});
	}

	/**
	 * format the call date into some special formats.
	 * @param date
	 * @return formatted date string.
	 */
	private String ComputeDate(String date) {
		long callTime = Long.parseLong(date); // get the call time in millisecond.
		long newTime = new Date().getTime(); // get time distance.
		long duration = (newTime - callTime) / (1000 * 60); // convert the time difference into minutes.
		
		String value;
		// SimpleDateFormat sfd = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
		// Locale.getDefault());
		// String time = sfd.format(callTime);
		// Log.d(TAG, "[MyCursorAdapter--ComputeDate] time = " + time);
		
		// 进行判断拨打电话的距离现在的时间，然后进行显示说明
		// display with the time difference.
		if (duration < 60) { // less than an hour.
			value = duration + "分钟前";
		} else if (duration >= 60 && duration < MainActivity.DAY) { // bigger than an hour, less than a day.
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm",
					Locale.getDefault()); // time format.
			value = sdf.format(new Date(callTime)); // format the call time.

			// value = (duration / 60) + "小时前";
		} else if (duration >= MainActivity.DAY
				&& duration < MainActivity.DAY * 2) { // 1~2 days.
			value = "昨天";
		} else if (duration >= MainActivity.DAY * 2
				&& duration < MainActivity.DAY * 3) { // 2~3 days.
			value = "前天";
		} else if (duration <= MainActivity.DAY * 7) { // less than a week.
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd",
					Locale.getDefault());
			value = sdf.format(new Date(callTime));
		} else { // more than a week.
			value = (duration / MainActivity.DAY) + "天前";
		}
		return value;
	}
}