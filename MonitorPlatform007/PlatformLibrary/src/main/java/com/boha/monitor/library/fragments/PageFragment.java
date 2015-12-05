package com.boha.monitor.library.fragments;

/**
 * Interface for all fragments housed in viewPager
 * Created by aubreyM on 2014/05/20.
 */
public interface PageFragment {
    void animateHeroHeight();
    void setPageTitle(String title);
    String getPageTitle();
    void setThemeColors(int primaryColor, int darkColor);

}
