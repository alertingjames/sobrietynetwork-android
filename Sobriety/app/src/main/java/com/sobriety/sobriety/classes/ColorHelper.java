package com.sobriety.sobriety.classes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.sobriety.sobriety.R;

/**
 * Created by LGH419 on 9/5/2018.
 */

public class ColorHelper {
    @ColorInt
    public static int getRandomMaterialColor(@NonNull Context context) {
        TypedArray colors = context.getResources().obtainTypedArray(R.array.material_colors_marker);
        int index = (int) (Math.random() * colors.length());
        int color = colors.getColor(index, Color.BLACK);
        colors.recycle();
        return color;
    }
}
