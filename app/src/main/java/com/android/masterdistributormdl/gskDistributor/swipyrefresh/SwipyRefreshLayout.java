package com.android.masterdistributormdl.gskDistributor.swipyrefresh;



import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.android.masterdistributormdl.R;


public class SwipyRefreshLayout extends ViewGroup {
    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);
    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 50;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    public final String TAG = SwipyRefreshLayout.class.getSimpleName();
    private final DecelerateInterpolator mDecelerateInterpolator;
    private final int mTouchSlop;
    private final int mMediumAnimationDuration;
    private final float mSpinnerFinalOffset;
    protected int mFrom;
    protected int mOriginalOffsetTop;
    private View mTarget;
    private SwipyDirection mDirection;
    private boolean mBothDirection;
    private OnSwipyRefreshListener mListener;
    private boolean mRefreshing = false;
    private float mTotalDragDistance = -1;
    private int mCurrentTargetOffsetTop;
    private boolean mOriginalOffsetCalculated = false;
    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mScale;
    private boolean mReturningToStart;
    private CircleImageView mCircleView;
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };
    private int mCircleViewIndex = -1;
    private float mStartingScale;
    private MaterialDrawable mProgress;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mAlphaStartAnimation;
    private Animation mAlphaMaxAnimation;
    private Animation mScaleDownToStartAnimation;
    private boolean mNotify;
    private final AnimationListener mRefreshListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh(mDirection);
                    }
                }
            } else {
                mProgress.stop();
                mCircleView.setVisibility(View.GONE);
                setColorViewAlpha(MAX_ALPHA);
                if (mScale) {
                    setAnimationProgress(0);
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop, true);
                }
            }
            mCurrentTargetOffsetTop = mCircleView.getTop();
        }
    };
    private int mCircleWidth;
    private int mCircleHeight;
    private boolean mUsingCustomStart;
    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                switch (mDirection) {
                    case BOTTOM:
                        endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                        break;
                    case TOP:
                    default:
                        endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                        break;
                }
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mCircleView.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
        }
    };


    public SwipyRefreshLayout(Context context) {
        this(context, null);
    }


    public SwipyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(2f);
        int[] LAYOUT_ATTRS = new int[]{
                android.R.attr.enabled
        };
        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
        final TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SwipyRefreshLayout);
        SwipyDirection direction = SwipyDirection.getFromInt(a2.getInt(R.styleable.SwipyRefreshLayout_direction, 0));
        if (direction != SwipyDirection.BOTH) {
            mDirection = direction;
            mBothDirection = false;
        } else {
            mDirection = SwipyDirection.TOP;
            mBothDirection = true;
        }
        a2.recycle();

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);

        createProgressView();

        setChildrenDrawingOrderEnabled(true);
        mSpinnerFinalOffset = 64 * metrics.density;
    }

    private void setColorViewAlpha(int targetAlpha) {
        mCircleView.getBackground().setAlpha(targetAlpha);
        mProgress.setAlpha(targetAlpha);
    }

    public void setSize(int size) {
        if (size != MaterialDrawable.LARGE && size != MaterialDrawable.DEFAULT) {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == MaterialDrawable.LARGE) {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        mCircleView.setImageDrawable(null);
        mProgress.updateSizes(size);
        mCircleView.setImageDrawable(mProgress);
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        if (mCircleViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            return mCircleViewIndex;
        } else if (i >= mCircleViewIndex) {
            return i + 1;
        } else {
            return i;
        }
    }

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);
    }

    public void setOnRefreshListener(OnSwipyRefreshListener listener) {
        mListener = listener;
    }


    private void startScaleUpAnimation(AnimationListener listener) {
        mCircleView.setVisibility(View.VISIBLE);
        mProgress.setAlpha(MAX_ALPHA);
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleAnimation);
    }

    private void setAnimationProgress(float progress) {
        mCircleView.setScaleX(progress);
        mCircleView.setScaleY(progress);
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private void startScaleDownAnimation(AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(150);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        mAlphaStartAnimation = startAlphaAnimation(mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
    }

    private void startProgressAlphaMaxAnimation() {
        mAlphaMaxAnimation = startAlphaAnimation(mProgress.getAlpha(), MAX_ALPHA);
    }

    private Animation startAlphaAnimation(final int startingAlpha, final int endingAlpha) {
        if (mScale) {
            return null;
        }
        Animation alpha = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                mProgress.setAlpha((int) (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)));
            }
        };
        alpha.setDuration(300);
        mCircleView.setAnimationListener(null);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(alpha);
        return alpha;
    }

    public void setProgressBackgroundColor(int colorRes) {
        mCircleView.setBackgroundColor(colorRes);
        mProgress.setBackgroundColor(getResources().getColor(colorRes));
    }

    @Deprecated
    public void setColorScheme(int... colors) {
        setColorSchemeResources(colors);
    }


    public void setColorSchemeResources(int... colorResIds) {
        final Resources res = getResources();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(int... colors) {
        ensureTarget();
        mProgress.setColorSchemeColors(colors);
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                switch (mDirection) {
                    case BOTTOM:
                        endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                        break;
                    case TOP:
                    default:
                        endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                        break;
                }
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop, true);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false);
        }
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mCircleView)) {
                    mTarget = child;
                    break;
                }
            }
        }
        if (mTotalDragDistance == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTotalDragDistance = (int) Math.min(
                        ((View) getParent()).getHeight() * .6f,
                        120 * metrics.density);
            }
        }
    }


    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));
        if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;

            switch (mDirection) {
                case BOTTOM:
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                    break;
                case TOP:
                default:
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                    break;
            }
        }
        mCircleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mCircleView) {
                mCircleViewIndex = index;
                break;
            }
        }
    }


    public boolean canChildScrollUp() {
        return mTarget.canScrollVertically(-1);
    }

    public boolean canChildScrollDown() {
        return mTarget.canScrollVertically(1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = ev.getActionMasked();

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        switch (mDirection) {
            case BOTTOM:
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollDown()) || mRefreshing) {
                    return false;
                }
                break;
            case TOP:
            default:
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollUp()) || mRefreshing) {
                    return false;
                }
                break;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                if (mBothDirection) {
                    if (y > mInitialDownY) {
                        setRawDirection(SwipyDirection.TOP);
                    } else if (y < mInitialDownY) {
                        setRawDirection(SwipyDirection.BOTTOM);
                    }
                    if ((mDirection == SwipyDirection.BOTTOM && canChildScrollDown())
                            || (mDirection == SwipyDirection.TOP && canChildScrollUp())) {
                        mInitialDownY = y;
                        return false;
                    }
                }
                float yDiff;
                switch (mDirection) {
                    case BOTTOM:
                        yDiff = mInitialDownY - y;
                        break;
                    case TOP:
                    default:
                        yDiff = y - mInitialDownY;
                        break;
                }
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    switch (mDirection) {
                        case BOTTOM:
                            mInitialMotionY = mInitialDownY - mTouchSlop;
                            break;
                        case TOP:
                        default:
                            mInitialMotionY = mInitialDownY + mTouchSlop;
                            break;
                    }
                    mIsBeingDragged = true;
                    mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = ev.findPointerIndex(activePointerId);
        if (index < 0) {
            return -1;
        }
        return ev.getY(index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            final int action = ev.getActionMasked();
            if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
                mReturningToStart = false;
            }
            switch (mDirection) {
                case BOTTOM:
                    if (!isEnabled() || mReturningToStart || canChildScrollDown() || mRefreshing) {
                        return false;
                    }
                    break;
                case TOP:
                default:
                    if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
                        return false;
                    }
                    break;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = ev.getPointerId(0);
                    mIsBeingDragged = false;
                    break;

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (pointerIndex < 0) {
                        return false;
                    }

                    final float y = ev.getY(pointerIndex);

                    float overscrollTop;
                    switch (mDirection) {
                        case BOTTOM:
                            overscrollTop = (mInitialMotionY - y) * DRAG_RATE;
                            break;
                        case TOP:
                        default:
                            overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                            break;
                    }
                    if (mIsBeingDragged) {
                        mProgress.showArrow(true);
                        float originalDragPercent = overscrollTop / mTotalDragDistance;
                        if (originalDragPercent < 0) {
                            return false;
                        }
                        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
                        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
                        float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset - mOriginalOffsetTop : mSpinnerFinalOffset;
                        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
                        float extraMove = (slingshotDist) * tensionPercent * 2;

                        int targetY;
                        if (mDirection == SwipyDirection.TOP) {
                            targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                        } else {
                            targetY = mOriginalOffsetTop - (int) ((slingshotDist * dragPercent) + extraMove);
                        }
                        // where 1.0f is a full circle
                        if (mCircleView.getVisibility() != View.VISIBLE) {
                            mCircleView.setVisibility(View.VISIBLE);
                        }
                        if (!mScale) {
                            mCircleView.setScaleX(1f);
                            mCircleView.setScaleY(1f);
                        }
                        if (overscrollTop < mTotalDragDistance) {
                            if (mScale) {
                                setAnimationProgress(overscrollTop / mTotalDragDistance);
                            }
                            if (mProgress.getAlpha() > STARTING_PROGRESS_ALPHA && !isAnimationRunning(mAlphaStartAnimation)) {
                                startProgressAlphaStartAnimation();
                            }
                            float strokeStart = (float) (adjustedPercent * .8f);
                            mProgress.setStartEndTrim(0f, Math.min(.8f, strokeStart));
                            mProgress.setArrowScale(Math.min(1f, adjustedPercent));
                        } else {
                            if (mProgress.getAlpha() < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                                startProgressAlphaMaxAnimation();
                            }
                        }
                        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
                        mProgress.setProgressRotation(rotation);
                        setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop, true /* requires update */);
                    }
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    final int index = ev.getActionIndex();
                    mActivePointerId = ev.getPointerId(index);
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (mActivePointerId == INVALID_POINTER) {
                        return false;
                    }
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float y = ev.getY(pointerIndex);

                    float overscrollTop;
                    switch (mDirection) {
                        case BOTTOM:
                            overscrollTop = (mInitialMotionY - y) * DRAG_RATE;
                            break;
                        case TOP:
                        default:
                            overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                            break;
                    }
                    mIsBeingDragged = false;
                    if (overscrollTop > mTotalDragDistance) {
                        setRefreshing(true, true);
                    } else {
                        mRefreshing = false;
                        mProgress.setStartEndTrim(0f, 0f);
                        AnimationListener listener = null;
                        if (!mScale) {
                            listener = new AnimationListener() {

                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if (!mScale) {
                                        startScaleDownAnimation(null);
                                    }
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                            };
                        }
                        animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
                        mProgress.showArrow(false);
                    }
                    mActivePointerId = INVALID_POINTER;
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured during SwipyRefreshLayout onTouchEvent " + e.toString());
        }

        return true;
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(200);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        if (mScale) {
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(200);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mCircleView.setAnimationListener(listener);
            }
            mCircleView.clearAnimation();
            mCircleView.startAnimation(mAnimateToStartPosition);
        }
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mCircleView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private void startScaleDownReturnToStartAnimation(int from, AnimationListener listener) {
        mFrom = from;

        mStartingScale = mCircleView.getScaleX();

        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(150);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownToStartAnimation);
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mCircleView.bringToFront();
        mCircleView.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mCircleView.getTop();
        if (requiresUpdate) {
            invalidate();
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public SwipyDirection getDirection() {
        return mBothDirection ? SwipyDirection.BOTH : mDirection;
    }

    public void setDirection(SwipyDirection direction) {
        if (direction == SwipyDirection.BOTH) {
            mBothDirection = true;
        } else {
            mBothDirection = false;
            mDirection = direction;
        }

        switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                break;
        }
    }

    private void setRawDirection(SwipyDirection direction) {
        if (mDirection == direction) {
            return;
        }

        mDirection = direction;
        switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                break;
        }
    }

    public CircleImageView getCircleView() {
        return mCircleView;
    }


}
