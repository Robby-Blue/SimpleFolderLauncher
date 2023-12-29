package me.robbyblue.mylauncher;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListener extends GestureDetector.SimpleOnGestureListener {

    SwipeCallback callback;

    public void setOnSwipeListener(SwipeCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return callback.onSwipe(event1, event2, velocityX, velocityY);
    }

    public interface SwipeCallback {
        boolean onSwipe(MotionEvent event1, MotionEvent event2,
                     float velocityX, float velocityY);
    }

}
