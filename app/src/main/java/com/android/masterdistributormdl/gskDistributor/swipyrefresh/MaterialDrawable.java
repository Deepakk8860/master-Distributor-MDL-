package com.android.masterdistributormdl.gskDistributor.swipyrefresh;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


class MaterialDrawable extends Drawable implements Animatable {
    static final int LARGE = 0;
    static final int DEFAULT = 1;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator END_CURVE_INTERPOLATOR = new EndCurveInterpolator();
    private static final Interpolator START_CURVE_INTERPOLATOR = new StartCurveInterpolator();
    private static final Interpolator EASE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final int CIRCLE_DIAMETER = 40;
    private static final float CENTER_RADIUS = 8.75f; //should add up to 10 when + stroke_width
    private static final float STROKE_WIDTH = 2.5f;
    private static final int CIRCLE_DIAMETER_LARGE = 45;
    private static final float CENTER_RADIUS_LARGE = 12.5f;
    private static final float STROKE_WIDTH_LARGE = 3f;
    private static final int ANIMATION_DURATION = 1000 * 80 / 60;
    private static final float NUM_POINTS = 5f;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 5;
    private static final float ARROW_OFFSET_ANGLE = 5;
    private static final int ARROW_WIDTH_LARGE = 12;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final float MAX_PROGRESS_ARC = .8f;
    private final int[] COLORS = new int[]{Color.BLACK};
    private final ArrayList<Animation> mAnimators = new ArrayList<Animation>();
    private final Ring mRing;
    private final Callback mCallback = new Callback() {
        @Override
        public void invalidateDrawable(Drawable d) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {
            unscheduleSelf(what);
        }
    };
    private boolean stopped;
    private float mRotation;
    private Resources mResources;
    private View mParent;
    private Animation mAnimation;
    private float mRotationCount;
    private double mWidth;
    private double mHeight;
    private Animation mFinishAnimation;

    public MaterialDrawable(Context context, View parent) {
        mParent = parent;
        mResources = context.getResources();
        mRing = new Ring(mCallback);
        mRing.setColors(COLORS);
        updateSizes(DEFAULT);
        setupAnimators();
    }

    private void setSizeParameters(double progressCircleWidth, double progressCircleHeight,
                                   double centerRadius, double strokeWidth, float arrowWidth, float arrowHeight) {
        final Ring ring = mRing;
        final DisplayMetrics metrics = mResources.getDisplayMetrics();
        final float screenDensity = metrics.density;
        mWidth = progressCircleWidth * screenDensity;
        mHeight = progressCircleHeight * screenDensity;
        ring.setStrokeWidth((float) strokeWidth * screenDensity);
        ring.setCenterRadius(centerRadius * screenDensity);
        ring.setColorIndex(0);
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity);
        ring.setInsets((int) mWidth, (int) mHeight);
    }

    public void updateSizes(@ProgressDrawableSize int size) {
        if (size == LARGE) {
            setSizeParameters(CIRCLE_DIAMETER_LARGE, CIRCLE_DIAMETER_LARGE, CENTER_RADIUS_LARGE,
                    STROKE_WIDTH_LARGE, ARROW_WIDTH_LARGE, ARROW_HEIGHT_LARGE);
        } else {
            setSizeParameters(CIRCLE_DIAMETER, CIRCLE_DIAMETER, CENTER_RADIUS, STROKE_WIDTH,
                    ARROW_WIDTH, ARROW_HEIGHT);
        }
    }

    public void showArrow(boolean show) {
        mRing.setShowArrow(show);
    }


    public void setArrowScale(float scale) {
        mRing.setArrowScale(scale);
    }


    public void setStartEndTrim(float startAngle, float endAngle) {
        mRing.setStartTrim(startAngle);
        mRing.setEndTrim(endAngle);
    }

    public void setProgressRotation(float rotation) {
        mRing.setRotation(rotation);
    }

    public void setBackgroundColor(int color) {
        mRing.setBackgroundColor(color);
    }


    public void setColorSchemeColors(int... colors) {
        mRing.setColors(colors);
        mRing.setColorIndex(0);
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) mWidth;
    }

    @Override
    public void draw(Canvas c) {
        final Rect bounds = getBounds();
        final int saveCount = c.save();
        c.rotate(mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        mRing.draw(c, bounds);
        c.restoreToCount(saveCount);
    }

    public int getAlpha() {
        return mRing.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        mRing.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mRing.setColorFilter(colorFilter);
    }

    @SuppressWarnings("unused")
    private float getRotation() {
        return mRotation;
    }

    @SuppressWarnings("unused")
    void setRotation(float rotation) {
        mRotation = rotation;
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isRunning() {
        final ArrayList<Animation> animators = mAnimators;
        final int N = animators.size();
        for (int i = 0; i < N; i++) {
            final Animation animator = animators.get(i);
            if (animator.hasStarted() && !animator.hasEnded()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        stopped = false;
        mAnimation.reset();
        mRing.storeOriginals();
        // Already showing some part of the ring
        if (mRing.getEndTrim() != mRing.getStartTrim()) {
            mParent.startAnimation(mFinishAnimation);
        } else {
            mRing.setColorIndex(0);
            mRing.resetOriginals();
            mParent.startAnimation(mAnimation);
        }
    }

    @Override
    public void stop() {
        stopped = true;
        mParent.clearAnimation();
        setRotation(0);
        mRing.setShowArrow(false);
        mRing.setColorIndex(0);
        mRing.resetOriginals();
    }

    private void setupAnimators() {
        final Ring ring = mRing;
        final Animation finishRingAnimation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetRotation = (float) (Math.floor(ring.getStartingRotation() / MAX_PROGRESS_ARC) + 1f);
                final float startTrim = ring.getStartingStartTrim() + (ring.getStartingEndTrim() - ring.getStartingStartTrim()) * interpolatedTime;
                ring.setStartTrim(startTrim);
                final float rotation = ring.getStartingRotation() + ((targetRotation - ring.getStartingRotation()) * interpolatedTime);
                ring.setRotation(rotation);
                ring.setArrowScale(1 - interpolatedTime);
            }
        };
        finishRingAnimation.setInterpolator(EASE_INTERPOLATOR);
        finishRingAnimation.setDuration(ANIMATION_DURATION / 2);
        finishRingAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (stopped) {
                    return;
                }
                ring.goToNextColor();
                ring.storeOriginals();
                ring.setShowArrow(false);
                mParent.startAnimation(mAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        final Animation animation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                final float minProgressArc = (float) Math.toRadians(ring.getStrokeWidth() / (2 * Math.PI * ring.getCenterRadius()));
                final float startingEndTrim = ring.getStartingEndTrim();
                final float startingTrim = ring.getStartingStartTrim();
                final float startingRotation = ring.getStartingRotation();

                // Offset the minProgressArc to where the endTrim is located.
                final float minArc = MAX_PROGRESS_ARC - minProgressArc;
                final float endTrim = startingEndTrim + (minArc * START_CURVE_INTERPOLATOR.getInterpolation(interpolatedTime));
                ring.setEndTrim(endTrim);

                final float startTrim = startingTrim + (MAX_PROGRESS_ARC * END_CURVE_INTERPOLATOR.getInterpolation(interpolatedTime));
                ring.setStartTrim(startTrim);

                final float rotation = startingRotation + (0.25f * interpolatedTime);
                ring.setRotation(rotation);

                float groupRotation = ((720.0f / NUM_POINTS) * interpolatedTime) + (720.0f * (mRotationCount / NUM_POINTS));
                setRotation(groupRotation);
            }
        };
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(LINEAR_INTERPOLATOR);
        animation.setDuration(ANIMATION_DURATION);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mRotationCount = 0;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                ring.storeOriginals();
                ring.goToNextColor();
                ring.setStartTrim(ring.getEndTrim());
                mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
            }
        });
        mFinishAnimation = finishRingAnimation;
        mAnimation = animation;
    }

    @Retention(RetentionPolicy.CLASS)
    @IntDef({LARGE, DEFAULT})
    public @interface ProgressDrawableSize {
    }

    private static class Ring {
        private final RectF mTempBounds = new RectF();
        private final Paint mPaint = new Paint();
        private final Paint mArrowPaint = new Paint();

        private final Callback mCallback;
        private final Paint mCirclePaint = new Paint();
        private float mStartTrim = 0.0f;
        private float mEndTrim = 0.0f;
        private float mRotation = 0.0f;
        private float mStrokeWidth = 5.0f;
        private float mStrokeInset = 2.5f;
        private int[] mColors;
        // mColorIndex represents the offset into the available mColors that the
        // progress circle should currently display. As the progress circle is
        // animating, the mColorIndex moves by one to the next available color.
        private int mColorIndex;
        private float mStartingStartTrim;
        private float mStartingEndTrim;
        private float mStartingRotation;
        private boolean mShowArrow;
        private Path mArrow;
        private float mArrowScale;
        private double mRingCenterRadius;
        private int mArrowWidth;
        private int mArrowHeight;
        private int mAlpha;
        private int mBackgroundColor;

        public Ring(Callback callback) {
            mCallback = callback;

            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Style.STROKE);

            mArrowPaint.setStyle(Style.FILL);
            mArrowPaint.setAntiAlias(true);
        }

        public void setBackgroundColor(int color) {
            mBackgroundColor = color;
        }


        public void setArrowDimensions(float width, float height) {
            mArrowWidth = (int) width;
            mArrowHeight = (int) height;
        }

        public void draw(Canvas c, Rect bounds) {
            final RectF arcBounds = mTempBounds;
            arcBounds.set(bounds);
            arcBounds.inset(mStrokeInset, mStrokeInset);

            final float startAngle = (mStartTrim + mRotation) * 360;
            final float endAngle = (mEndTrim + mRotation) * 360;
            float sweepAngle = endAngle - startAngle;

            mPaint.setColor(mColors[mColorIndex]);
            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);

            drawTriangle(c, startAngle, sweepAngle, bounds);

            if (mAlpha < 255) {
                mCirclePaint.setColor(mBackgroundColor);
                mCirclePaint.setAlpha(255 - mAlpha);
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), bounds.width() / 2,
                        mCirclePaint);
            }
        }

        private void drawTriangle(Canvas c, float startAngle, float sweepAngle, Rect bounds) {
            if (mShowArrow) {
                if (mArrow == null) {
                    mArrow = new Path();
                    mArrow.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    mArrow.reset();
                }


                float inset = (int) mStrokeInset / 2 * mArrowScale;
                float x = (float) (mRingCenterRadius * Math.cos(0) + bounds.exactCenterX());
                float y = (float) (mRingCenterRadius * Math.sin(0) + bounds.exactCenterY());


                mArrow.moveTo(0, 0);
                mArrow.lineTo(mArrowWidth * mArrowScale, 0);
                mArrow.lineTo((mArrowWidth * mArrowScale / 2), (mArrowHeight * mArrowScale));
                mArrow.offset(x - inset, y);
                mArrow.close();
                // draw a triangle
                mArrowPaint.setColor(mColors[mColorIndex]);
                c.rotate(startAngle + sweepAngle - ARROW_OFFSET_ANGLE, bounds.exactCenterX(),
                        bounds.exactCenterY());
                c.drawPath(mArrow, mArrowPaint);
            }
        }


        public void setColors(@NonNull int[] colors) {
            mColors = colors;
            setColorIndex(0);
        }

        public void setColorIndex(int index) {
            mColorIndex = index;
        }


        public void goToNextColor() {
            mColorIndex = (mColorIndex + 1) % (mColors.length);
        }

        public void setColorFilter(ColorFilter filter) {
            mPaint.setColorFilter(filter);
            invalidateSelf();
        }

        public int getAlpha() {
            return mAlpha;
        }

        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }

        @SuppressWarnings("unused")
        public float getStrokeWidth() {
            return mStrokeWidth;
        }

        public void setStrokeWidth(float strokeWidth) {
            mStrokeWidth = strokeWidth;
            mPaint.setStrokeWidth(strokeWidth);
            invalidateSelf();
        }

        @SuppressWarnings("unused")
        public float getStartTrim() {
            return mStartTrim;
        }

        @SuppressWarnings("unused")
        public void setStartTrim(float startTrim) {
            mStartTrim = startTrim;
            invalidateSelf();
        }

        public float getStartingStartTrim() {
            return mStartingStartTrim;
        }

        public float getStartingEndTrim() {
            return mStartingEndTrim;
        }

        @SuppressWarnings("unused")
        public float getEndTrim() {
            return mEndTrim;
        }

        @SuppressWarnings("unused")
        public void setEndTrim(float endTrim) {
            mEndTrim = endTrim;
            invalidateSelf();
        }

        @SuppressWarnings("unused")
        public float getRotation() {
            return mRotation;
        }

        @SuppressWarnings("unused")
        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        public void setInsets(int width, int height) {
            final float minEdge = (float) Math.min(width, height);
            float insets;
            if (mRingCenterRadius <= 0 || minEdge < 0) {
                insets = (float) Math.ceil(mStrokeWidth / 2.0f);
            } else {
                insets = (float) (minEdge / 2.0f - mRingCenterRadius);
            }
            mStrokeInset = insets;
        }

        @SuppressWarnings("unused")
        public float getInsets() {
            return mStrokeInset;
        }

        public double getCenterRadius() {
            return mRingCenterRadius;
        }

        public void setCenterRadius(double centerRadius) {
            mRingCenterRadius = centerRadius;
        }

        public void setShowArrow(boolean show) {
            if (mShowArrow != show) {
                mShowArrow = show;
                invalidateSelf();
            }
        }

        public void setArrowScale(float scale) {
            if (scale != mArrowScale) {
                mArrowScale = scale;
                invalidateSelf();
            }
        }


        public float getStartingRotation() {
            return mStartingRotation;
        }


        public void storeOriginals() {
            mStartingStartTrim = mStartTrim;
            mStartingEndTrim = mEndTrim;
            mStartingRotation = mRotation;
        }

        public void resetOriginals() {
            mStartingStartTrim = 0;
            mStartingEndTrim = 0;
            mStartingRotation = 0;
            setStartTrim(0);
            setEndTrim(0);
            setRotation(0);
        }

        private void invalidateSelf() {
            mCallback.invalidateDrawable(null);
        }
    }

    /**
     * Squishes the interpolation curve into the second half of the animation.
     */
    private static class EndCurveInterpolator extends AccelerateDecelerateInterpolator {
        @Override
        public float getInterpolation(float input) {
            return super.getInterpolation(Math.max(0, (input - 0.5f) * 2.0f));
        }
    }

    /**
     * Squishes the interpolation curve into the first half of the animation.
     */
    private static class StartCurveInterpolator extends AccelerateDecelerateInterpolator {
        @Override
        public float getInterpolation(float input) {
            return super.getInterpolation(Math.min(1, input * 2.0f));
        }
    }
}
