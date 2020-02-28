package com.irrigation.wifilocation.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by laxmi on 27/4/18.
 */

public class CustomTextViewLightBold extends android.support.v7.widget.AppCompatTextView {
    public CustomTextViewLightBold(Context context) {
        super(context);
        init(context);
    }

    public CustomTextViewLightBold(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTextViewLightBold(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),"fonts/cambria.ttf");
        setTypeface(tf, Typeface.BOLD);
//        setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
    }

}
