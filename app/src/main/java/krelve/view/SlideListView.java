package krelve.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by wwjun.wang on 2015/9/18.
 */
public class SlideListView extends ListView {
    private Scroller scroller;
    private VelocityTracker velocityTracker;
    private int curPos;
    private View item;
    private int startX, startY;
    private int mTouchSlop;
    private int screenWidth;
    private int alphaWidth;
    private static final int BORDER = 1000;
    private boolean isSliding = false;
    private ScrollDirection scrollDirection;
    private OnRemovedListener mOnRemovedListener;

    public void setOnRemovedListener(OnRemovedListener onRemovedListener) {
        this.mOnRemovedListener = onRemovedListener;
    }

    private enum ScrollDirection {
        LEFT, RIGHT, ORIGIN
    }


    public SlideListView(Context context) {
        super(context);
        init(context);
    }

    public SlideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public SlideListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        alphaWidth = screenWidth / 3 * 2;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void handleActionUp(MotionEvent ev) {
        if (item != null && isSliding && scroller.isFinished()) {
            int velocityX = getXVelocity();
            if (velocityX > BORDER) {
                scrollToRight();
            } /*else if (velocityX < -BORDER) {
                        scrollToLeft();
                    }*/ else {
                scrollToOrigin();
            }
            removeVelocityTracker();
            isSliding = false;
        }
    }

    private void handleActionMove(MotionEvent ev) {
        if (item != null && scroller.isFinished()) {
            if ((Math.abs(ev.getX() - startX) > mTouchSlop
                    && (Math.abs(ev.getY() - startY) < mTouchSlop))) {
                isSliding = true;
                addVelocityTracker(ev);
                MotionEvent cancelEvent = MotionEvent.obtain(ev);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                        (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                onTouchEvent(cancelEvent);
                int curX = (int) ev.getX();
                int deltaX = startX - curX;
                startX = curX;
                item.scrollBy(deltaX, 0);
                item.setAlpha(1f - Math.abs(item.getScrollX()) * 1.0f / alphaWidth);
            }
        }


    }

    private void handleActionDown(MotionEvent ev) {
        if (scroller.isFinished()) {
            startX = (int) ev.getX();
            startY = (int) ev.getY();
            curPos = pointToPosition(startX, startY);
            if (curPos == AdapterView.INVALID_POSITION) {//-1
                item = null;
                return;
            }
            item = getChildAt(curPos - getFirstVisiblePosition());
        }

    }


    private void scrollToOrigin() {
        scrollDirection = ScrollDirection.ORIGIN;
        scroller.startScroll(item.getScrollX(), 0, 0 - item.getScrollX(), 0);
        postInvalidate();
    }

    private void scrollToLeft() {
        scrollDirection = ScrollDirection.LEFT;
        int deltaX = screenWidth - item.getScrollX();
        scroller.startScroll(item.getScrollX(), 0, deltaX, 0);
        postInvalidate();
    }

    private void scrollToRight() {
        scrollDirection = ScrollDirection.RIGHT;
        //getScrollX()获得的值是 起点坐标减去当前坐标
        int deltaX = item.getScrollX() + screenWidth;
        scroller.startScroll(item.getScrollX(), 0, -deltaX, 0);
        postInvalidate();
    }

    private void addVelocityTracker(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
    }

    private int getXVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        return (int) velocityTracker.getXVelocity();
    }

    private void removeVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        //调用scroller后，computScrollOffset的返回值为true
        if (scroller.computeScrollOffset()) {
            item.scrollTo(scroller.getCurrX(), 0);
            item.setAlpha(1f - Math.abs(item.getScrollX()) * 1.0f / alphaWidth);

            postInvalidate();
            if (scroller.isFinished() && scrollDirection != ScrollDirection.ORIGIN) {
                item.scrollTo(0, 0);
                final ViewGroup.LayoutParams lp = item.getLayoutParams();
                ValueAnimator valueAnimator = ValueAnimator.ofInt(item.getHeight(), 0).setDuration(300);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        lp.height = (int) animation.getAnimatedValue();
                        item.setLayoutParams(lp);
                    }

                });
                valueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        item.setAlpha(1f);
                        if (mOnRemovedListener != null) {
                            mOnRemovedListener.onRemoveItem(curPos);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }

                });
                valueAnimator.start();
            }
        }
    }

    public interface OnRemovedListener {
        void onRemoveItem(int position);
    }
}
