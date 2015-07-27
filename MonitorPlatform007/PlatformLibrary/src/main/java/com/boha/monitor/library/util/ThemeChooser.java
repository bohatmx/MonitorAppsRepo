package com.boha.monitor.library.util;

import android.app.Activity;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.platform.library.R;


public class ThemeChooser {


    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void setTheme(Activity activity) {

        int theme = SharedUtil.getThemeSelection(activity);
        Log.e("ThemeChooser","### theme about to be applied: " + theme);
        switch (theme) {

            case MonApp.THEME_BLUE:
                activity.setTheme(R.style.BlueThemeTwo);
                break;
            case MonApp.THEME_INDIGO:
                activity.setTheme(R.style.IndigoTheme);
                break;
            case MonApp.THEME_RED:
                activity.setTheme(R.style.RedTheme);
                break;
            case MonApp.THEME_TEAL:
                activity.setTheme(R.style.TealTheme);
                break;
            case MonApp.THEME_BLUE_GRAY:
                activity.setTheme(R.style.BlueGrayTheme);
                break;
            case MonApp.THEME_ORANGE:
                activity.setTheme(R.style.OrangeTheme);
                break;
            case MonApp.THEME_PINK:
                activity.setTheme(R.style.PinkTheme);
                break;
            case MonApp.THEME_CYAN:
                activity.setTheme(R.style.CyanTheme);
                break;
            case MonApp.THEME_GREEN:
                activity.setTheme(R.style.GreenTheme);
                break;
            case MonApp.THEME_GREY:
                activity.setTheme(R.style.GreyTheme);
                break;
            case MonApp.THEME_LIGHT_GREEN:
                activity.setTheme(R.style.LightGreenTheme);
                break;
            case MonApp.THEME_LIME:
                activity.setTheme(R.style.LimeTheme);
                break;
            case MonApp.THEME_PURPLE:
                activity.setTheme(R.style.PurpleTheme);
                break;
            case MonApp.THEME_AMBER:
                activity.setTheme(R.style.AmberTheme);
                break;
            case MonApp.THEME_BROWN:
                activity.setTheme(R.style.BrownTheme);
                break;
            default:
                Log.d("ThemeChooser", "### no theme selected, none set");
                break;
        }

    }
}