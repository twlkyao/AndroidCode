package com.twlkyao.photowall;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {

    private Set<BitmapWorkerTask> taskCollection; // Record all the tasks being downloaded or waiting for download.
  
    /**
     * Cache all the downloaded photos, remove the lru photos when the memory reaches the maxium.
     */
    private LruCache<String, Bitmap> mMemoryCache;
  
    private GridView mPhotoWall; // GridView.
  
    private int mFirstVisibleItem; // The index of the first visible photo.
  
    private int mVisibleItemCount; // The amount of the photos visible in the screen.
  
    private boolean isFirstEnter = true; // Indicate whether this is the first time open this application, in case of the photo loading failed.
  
    public PhotoWallAdapter(Context context, int textViewResourceId, String[] objects,
            GridView photoWall) {
        super(context, textViewResourceId, objects);
        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();
        
        int maxMemory = (int) Runtime.getRuntime().maxMemory(); // Get the max heap memory the application can expand to.
        int cacheSize = maxMemory / 8; // Set the memory to use be one eighth of the max memory.
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) { // Return the minium memory that can hold the bitmap.
            	System.out.println(bitmap.getByteCount() + "");
                return bitmap.getByteCount();
            }
        };  
        mPhotoWall.setOnScrollListener(this); // Set listener for the GridView.
    }
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = getItem(position); // Get the url of the picture.
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout, null);
        } else {
            view = convertView;
        }
        final ImageView photo = (ImageView) view.findViewById(R.id.photo); // Find the ImageView.
        photo.setTag(url); // Set tag for the ImageView, in case of disorder when asynchronously loading.
        setImageView(url, photo); // Set image.
        return view;
    }
  
    /**
     * Set photo for the ImageView, first retrieve photo from the LruCache,
     * set the photo on the ImageView, set a default photo if there is no cache.
     * @param imageUrl The url of the photo, as a key.
     * @param imageView The widget to display a photo.
     */
    private void setImageView(String imageUrl, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);  
        if (bitmap != null) {  
            imageView.setImageBitmap(bitmap);
        } else {  
            imageView.setImageResource(R.drawable.empty_photo);
        }
    }
  
    /**
     * Add a bitmap to the LruCache only when the bitmap is not in the LruCache.
     * @param key The key, here is the url of the bitmap.
     * @param bitmap The value, here is the bitmap object downloaded from the url represented by the key.
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
  
    /**
     * Retrieve a photo from the LruCache.
     * @param key The url of the photo.
     * @return The corresponding phtoto or null, if the photo is not cached.
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key); // Retrieve the photo.
    }
  
    /**
     * The inherits method from ArrayAdapter, called while the list view or grid view is being scrolled.
     */
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) { // Only load photos when the GridView is not scrolling.
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        } else {
            cancelAllTasks(); // Cancel all the tasks when the GridView is scrolling.
        }
    }
  
    /**
     * The inherits method from ArrayAdapter, called after the scorlled has completed.
     * @param view The view whose scroll state is being reported.
     * @param firstVisableItem The index of the first visible cell (ignore if visibleItemCount == 0). 
     * @param visibleItemCount The number of visible cells.
     * @param totalItemCount The number of items in the list adaptor.
     */
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {  
        mFirstVisibleItem = firstVisibleItem; // First visible item index.
        mVisibleItemCount = visibleItemCount; // The amount of visible items.
        // Deal with the first time of entering the application, because the onScrollStateChanged() method
        // will not be called the first time.
        if (isFirstEnter && visibleItemCount > 0) {
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }
  
    /** 
     * Load bitmap, only download the photo when the cache misses, called the first time
     * application started or when the GridView is scrolled.
     * @param firstVisibleItem The index of the first visible ImageView.
     * @param visibleItemCount The amount of ImageView visible on the screen.
     */  
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                String imageUrl = Images.imageThumbUrls[i];
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap == null) { // Download the photo when the cache misses.
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task); // Add to task collection.
                    task.execute(imageUrl); // Execute the task.
                } else { // Set photo when the cache hits.
                    ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cancel all the downloading or waiting tasks.
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }
  
    /** 
     * Download photos asynchronously.
     */  
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
  
        private String imageUrl; // The url of the photo.
  
        /**
         * Download the photo in background.
         */
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0]; // Get the url.
            Bitmap bitmap = downloadBitmap(params[0]); // Get the bitmap.
            if (bitmap != null) {
                addBitmapToMemoryCache(params[0], bitmap); // Add the bitmap in the LruCache.
            }
            return bitmap;
        }
  
        /**
         * Set the photo after execute.
         */
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);  
            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl); // Find the corresponding ImageView according to the url.
            if (imageView != null && bitmap != null) { // Set image for the ImageView.
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this); // Remove from the task collection.
        }
  
        /**
         * Download bitmap from the specified url.
         * @param imageUrl The url of the bitmap to downloaded from.
         * @return The downloaded bitmap.
         */
        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {  
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection(); // Get a connection.
                con.setConnectTimeout(5 * 1000); // Set connection time.
                con.setReadTimeout(10 * 1000); // Set read time.
                con.setDoInput(true); // Allows input.
                con.setDoOutput(true); // Allows output.
                bitmap = BitmapFactory.decodeStream(con.getInputStream()); // Get the input stream from the connection and decode it to a bitmap.
            } catch (Exception e) {
                e.printStackTrace();
            } finally { // Close the connection.
                if (con != null) {
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}