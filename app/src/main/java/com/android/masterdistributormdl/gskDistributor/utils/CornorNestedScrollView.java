package com.android.masterdistributormdl.gskDistributor.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.android.masterdistributormdl.R;


public class CornorNestedScrollView extends NestedScrollView {
    private float radiusTopLeft = 0;
    private float radiusTopRight = 0;
    private float radiusBottomLeft = 0;
    private float radiusBottomRight = 0;
    private float radius = 0;
    private int backgroundColor = Color.TRANSPARENT;
    private int strockColor = 0;
    private float strockWidth = 0;

    public CornorNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public CornorNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornorLayout);
        radiusTopLeft = typedArray.getDimension(R.styleable.CornorLayout_radiusTopLeft, 0);
        radiusTopRight = typedArray.getDimension(R.styleable.CornorLayout_radiusTopRight, 0);
        radiusBottomLeft = typedArray.getDimension(R.styleable.CornorLayout_radiusBottomLeft, 0);
        radiusBottomRight = typedArray.getDimension(R.styleable.CornorLayout_radiusBottomRight, 0);
        radius = typedArray.getDimension(R.styleable.CornorLayout_radius, 0);
        backgroundColor = typedArray.getColor(R.styleable.CornorLayout_backgroundColor, Color.TRANSPARENT);
        strockColor = typedArray.getColor(R.styleable.CornorLayout_strokeColor, Color.TRANSPARENT);
        strockWidth = typedArray.getDimension(R.styleable.CornorLayout_strokeWidth, 0);
        setDrawable();
    }

    public CornorNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStrockColor(Integer color) {
        strockColor = color;
        setDrawable();
    }

    public void setStrockWidth(Integer width) {
        strockWidth = width;
        setDrawable();
    }

    public void setRadius(Integer radiusTopLeft, Integer radiusTopRight, Integer radiusBottomLeft, Integer radiusBottomRight) {
        this.radiusTopLeft = radiusTopLeft;
        this.radiusTopRight = radiusTopRight;
        this.radiusBottomLeft = radiusBottomLeft;
        this.radiusBottomRight = radiusBottomRight;
        setDrawable();
    }

    private void setDrawable() {
        GradientDrawable drawable = new GradientDrawable();

        if (radius > 0) {
            drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        } else {
            drawable.setCornerRadii(new float[]{radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, radiusBottomRight, radiusBottomRight, radiusBottomLeft, radiusBottomLeft});
        }
        drawable.setColor(backgroundColor);
        drawable.setStroke((int) strockWidth, strockColor);
        setBackground(drawable);
    }
}
