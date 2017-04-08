package hymob.developers.inspiron.buzzoff2;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Inspiron on 4/2/2017.
 */

public class OnSwipeTouchListener implements View.OnTouchListener{

  private final GestureDetector mGestureDetector;
    private final Context mContext;

    public  OnSwipeTouchListener(Context context){
        this.mContext = context;
        mGestureDetector = new GestureDetector(context,new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener{
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }
        }
        public void onSwipeRight() {
           // Toast.makeText(mContext,"right swipe",Toast.LENGTH_SHORT).show();
        }

        public void onSwipeLeft() {
        }

     /*   public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }*/

    }


