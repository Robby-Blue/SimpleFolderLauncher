package me.robbyblue.mylauncher;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class HomeGestureListener extends GestureDetector.SimpleOnGestureListener {

    SwipeCallback swipeCallback;
    DoubleTapCallback doubleTapCallback;

    public void setOnSwipeListener(SwipeCallback callback) {
        this.swipeCallback = callback;
    }

    public void setHomeGestureCallback(SwipeCallback callback, DoubleTapCallback doubleTapCallback) {
        this.swipeCallback = callback;
        this.doubleTapCallback = doubleTapCallback;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return swipeCallback.onSwipe(velocityX, velocityY);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (doubleTapCallback == null)
            return false;
        return doubleTapCallback.onDoubleTap();
    }

    public interface SwipeCallback {
        boolean onSwipe(float velocityX, float velocityY);
    }

    public interface DoubleTapCallback {
        boolean onDoubleTap();
    }

}
