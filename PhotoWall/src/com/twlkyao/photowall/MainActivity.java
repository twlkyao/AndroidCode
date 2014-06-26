package com.twlkyao.photowall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends Activity {

    private GridView mPhotoWall; // The GridView to display all the photos.
    private PhotoWallAdapter adapter; // The photo adapter.
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);
        mPhotoWall = (GridView) findViewById(R.id.photo_wall);  
        adapter = new PhotoWallAdapter(this, 0, Images.imageThumbUrls, mPhotoWall);  
        mPhotoWall.setAdapter(adapter); // Set adapter.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all the tasks when destroy.
        adapter.cancelAllTasks();
    }
}