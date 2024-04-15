    package com.example.budget_planner;

    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.SharedPreferences;
    import android.os.Bundle;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.app.AppCompatDelegate;

    public class BaseActivity extends AppCompatActivity {
        protected boolean isDarkMode;
        protected int selectedThemeId;

        protected static final String PREFS_NAME = "MyPrefs";
        protected static final String DARK_MODE_PREF = "darkMode";
        protected static final String SELECTED_THEME_PREF = "selectedThemeId";

        private BroadcastReceiver themeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean newDarkMode = intent.getBooleanExtra("isDarkMode", false);
                if (newDarkMode != isDarkMode) {
                    isDarkMode = newDarkMode;
                    setThemeMode(isDarkMode);
                    recreate();
                }
            }
        };

        private BroadcastReceiver themeIdChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int newThemeId = intent.getIntExtra("selectedThemeId", R.style.Base_Theme_Budget_Planner);
                if (newThemeId != selectedThemeId) {
                    selectedThemeId = newThemeId;
                    setSelectedTheme(selectedThemeId);
                    recreate();
                }
            }
        };

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            isDarkMode = prefs.getBoolean(DARK_MODE_PREF, false);
            selectedThemeId = prefs.getInt(SELECTED_THEME_PREF, R.style.Base_Theme_Budget_Planner);

            setThemeMode(isDarkMode);
        }

        @Override
        protected void onResume() {
            super.onResume();
            IntentFilter themeFilter = new IntentFilter("Theme_Change");
            registerReceiver(themeChangeReceiver, themeFilter);

            IntentFilter themeIdFilter = new IntentFilter("Theme_Id_Change");
            registerReceiver(themeIdChangeReceiver, themeIdFilter);
        }

        @Override
        protected void onPause() {
            super.onPause();
            unregisterReceiver(themeChangeReceiver);
            unregisterReceiver(themeIdChangeReceiver);
        }

        public void setThemeMode(boolean isDarkMode) {
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        public void setSelectedTheme(int themeId) {
            selectedThemeId = themeId;
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putInt(SELECTED_THEME_PREF, selectedThemeId);
            editor.apply();
            Intent intent = new Intent("Theme_Id_Change");
            intent.putExtra("selectedThemeId", themeId);
            sendBroadcast(intent);

        }
    }
