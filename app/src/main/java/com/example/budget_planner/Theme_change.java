package com.example.budget_planner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class Theme_change {
    private static final String THEME_PREFERENCE_KEY = "theme_preference";

    public static void toggleTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currentTheme = preferences.getInt(THEME_PREFERENCE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int newTheme;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            newTheme = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newTheme = AppCompatDelegate.MODE_NIGHT_YES;
        }

        // Save the new theme state
        preferences.edit().putInt(THEME_PREFERENCE_KEY, newTheme).apply();

        // Apply the new theme
        AppCompatDelegate.setDefaultNightMode(newTheme);
    }
}
