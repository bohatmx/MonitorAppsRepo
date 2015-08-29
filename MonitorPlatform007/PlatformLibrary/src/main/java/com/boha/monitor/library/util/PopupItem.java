package com.boha.monitor.library.util;

import android.graphics.drawable.Drawable;

/**
 * Created by aubreyM on 15/08/27.
 */
public class PopupItem {

    public PopupItem(int drawableID, String text) {
        this.drawableID = drawableID;
        this.text = text;
    }
    private int drawableID;
    private Drawable icon;
    private String text;

    public int getDrawableID() {
        return drawableID;
    }

    public void setDrawableID(int drawableID) {
        this.drawableID = drawableID;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
