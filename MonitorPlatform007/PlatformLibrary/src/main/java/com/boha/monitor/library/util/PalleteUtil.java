package com.boha.monitor.library.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.widget.TextView;

/**
 * Created by aubreyM on 15/08/24.
 */
public class PalleteUtil {


    public static void getColor(Resources resources, int resID, final TextView textView) {
        Bitmap image = BitmapFactory.decodeResource(resources,resID);
        Palette.from(image).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
//                    outerLayout.setBackgroundColor(vibrantSwatch.getRgb());
                    textView.setTextColor(vibrantSwatch.getTitleTextColor());
//                    bodyText.setTextColor(vibrantSwatch.getBodyTextColor());
                }
            }
        });
    }
}
