package com.android.masterdistributormdl.gskDistributor.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import com.android.masterdistributormdl.R;


public class PointTextView extends androidx.appcompat.widget.AppCompatTextView {

    private static Drawable drawable;
    private static float pointWidth = 5;
    private static float pointHeight = 5;

    public PointTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        style(context, attrs);
    }

    public PointTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        style(context, attrs);
    }


    public void setText2(String text) {
        String end_dot = "[end_dot]";
        String textValue = text + " " + end_dot;
        drawable.mutate();
        drawable.setBounds(0, 0, (int) pointWidth, (int) pointHeight);
        int start = textValue.indexOf(end_dot);
        SpannableStringBuilder ssb = new SpannableStringBuilder(textValue);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        ssb.setSpan(
                imageSpan,
                start, start + end_dot.getBytes().length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        );
        setText(ssb, BufferType.SPANNABLE);
    }


    private void style(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PointTextView);
        String text = (String) typedArray.getText(R.styleable.PointTextView_text);
        pointWidth = typedArray.getDimension(R.styleable.PointTextView_point_width, 0);
        pointHeight = typedArray.getDimension(R.styleable.PointTextView_point_height, 0);
        drawable = typedArray.getDrawable(R.styleable.PointTextView_src);
        setText2(text);
        typedArray.recycle();
    }

}
