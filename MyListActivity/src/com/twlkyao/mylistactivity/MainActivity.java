package com.twlkyao.mylistactivity;

import java.util.ArrayList;
import java.util.TreeSet;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {
  
	private MyCustomAdapter mAdapter;
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MyCustomAdapter();
        for (int i = 1; i < 50; i++) {
            mAdapter.addItem("item " + i);
            if (i % 4 == 0) { // Add an extra item to divide the list items.
                mAdapter.addSeparatorItem("separator " + i);
            }
        }
        setListAdapter(mAdapter);
    }
  
    private class MyCustomAdapter extends BaseAdapter {
  
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
  
        private ArrayList<String> mData = new ArrayList<String>(); // To store the first type of data.
        private LayoutInflater mInflater; // The LayoutInflater.
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>(); // To store the second type of data.
  
        public MyCustomAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
  
        public void addItem(final String item) { // Add first type.
            mData.add(item);
            notifyDataSetChanged(); // Notify the observer to change the view.
        }
  
        public void addSeparatorItem(final String item) { // Add first and second type.
            mData.add(item); // Add the item.
            // save separator position
            mSeparatorsSet.add(mData.size() - 1); // Record the index of the second type.
            notifyDataSetChanged(); // Notify the observer to change the view.
        }
  
        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }
  
        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
  
        @Override
        public int getCount() {
            return mData.size();
        }
  
        @Override
        public String getItem(int position) {
            return mData.get(position);
        }
  
        @Override
        public long getItemId(int position) {
            return position;
        }
  
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int type = getItemViewType(position);
            // System out the view information.
            System.out.println("getView " + position + " " + convertView + " type = " + type);
            if (convertView == null) { // The convertView is null(first created).
                holder = new ViewHolder();
                switch (type) { // Add different view according to different type.
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.item1, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.text);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.item2, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.textSeparator);
                        break;
                }
                convertView.setTag(holder);
            } else { // The convertview is already exist.
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(mData.get(position)); // Set data.
            return convertView;
        }
  
    }
  
    /**
     * The class to hold all the views in list item.
     */
    public static class ViewHolder {
        public TextView textView;
    }
}