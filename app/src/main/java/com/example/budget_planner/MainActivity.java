package com.example.budget_planner;

import com.example.budget_planner.R;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import android.Manifest;

public class MainActivity extends BaseActivity {
    private final int ID_HOME = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final int ID_CHART = 2;
    public static final int ID_TRANSACTIONS = 3;
    public static final int ID_CATEGORY = 4;


    private Toolbar toolbar;
    public static boolean isThemeChanged = false;
    private FloatingActionButton fab_theme; // Declare fab_theme at the class level

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);

        // Pass selectedthemeId to Home_Fragment
        Bundle bundle = new Bundle();
        bundle.putInt("selectedThemeId", selectedThemeId);
        fragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void updateTitle(int itemId) {
        String title = "";
        switch (itemId) {
            case ID_HOME:
                title = "Home";
                break;
            case ID_CHART:
                title = "Chart";
                break;
            case ID_TRANSACTIONS:
                title = "Transactions";
                break;
            case ID_CATEGORY:
                title = "Category";
                break;
        }
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Fetch the theme preference from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isDarkMode = prefs.getBoolean(DARK_MODE_PREF, false);
        selectedThemeId = prefs.getInt(SELECTED_THEME_PREF, R.style.Base_Theme_Budget_Planner); // Retrieve the selected theme

        // Apply the fetched theme
        setTheme(selectedThemeId);

        // Apply the fetched theme
        setThemeMode(isDarkMode);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        boolean nightMODE;
        fab_theme = findViewById(R.id.fab_theme); // Initialize fab_theme
        setFabIconBasedOnTheme(); // Set the initial icon based on the theme

        FloatingActionButton setting_fab = findViewById(R.id.settings);

        fab_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the theme
                if (selectedThemeId == R.style.Base_Theme_Budget_Planner) {
                    // Toggle the theme
                    isDarkMode = !isDarkMode;

                    // Save the theme state
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putBoolean(DARK_MODE_PREF, isDarkMode);
                    editor.apply();

                    // Broadcast the theme change
                    Intent intent = new Intent("Theme_Change");
                    intent.putExtra("isDarkMode", isDarkMode);
                    sendBroadcast(intent);

                    // Apply the new theme
                    setThemeMode(isDarkMode);


                } else {
                    // Show a toast message indicating inability to change theme
                    Toast.makeText(MainActivity.this, "Dark mode is only available for Default Theme", Toast.LENGTH_SHORT).show();
                }

            }
        });

        setting_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), page_setting.class);
                startActivity(intent);
            }
        });

        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomnavigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.outline_home_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CHART, R.drawable.graph_svgrepo_com));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_TRANSACTIONS, R.drawable.transactions_svgrepo_com));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CATEGORY, R.drawable.category_svgrepo_com));

        replaceFragment(new Home_Fragment());

        bottomNavigation.setOnShowListener(item -> {
            int itemId = item.getId();
            updateTitle(itemId);

            switch (itemId) {
                case ID_HOME:
                    replaceFragment(new Home_Fragment());
                    break;
                case ID_CHART:
                    replaceFragment(new Chart_Fragment());
                    break;
                case ID_TRANSACTIONS:
                    replaceFragment(new Transaction_Fragment());
                    break;
                case ID_CATEGORY:
                    replaceFragment(new Category_Fragment());
                    break;
            }
            return null;
        });

        bottomNavigation.show(ID_HOME, true);

        scheduleNotification();
        if (!checkNotificationPolicyPermission()) {
            requestNotificationPolicyPermission();
        }
    }


    private void setFabIconBasedOnTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Dark theme
            fab_theme.setImageResource(R.drawable.sun_svgrepo_com);
        } else {
            // Light theme
            fab_theme.setImageResource(R.drawable.moon_svgrepo_com);
        }
    }

    private boolean checkNotificationPolicyPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPolicyPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                // Handle permission denied scenario
            }
        }
    }
    public void updateBottomNavigation(int itemId) {
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomnavigation);
        bottomNavigation.show(itemId, true);
    }
    private void scheduleNotification() {
        // Create a Calendar object for 6 PM
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.set(Calendar.HOUR_OF_DAY, 18);
        notificationTime.set(Calendar.MINUTE, 0);
        notificationTime.set(Calendar.SECOND, 0);

        // If the current time is after 6 PM, schedule the notification for tomorrow
        if (Calendar.getInstance().after(notificationTime)) {
            notificationTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Calculate the delay until the notification time
        long delay = notificationTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        // Create a PeriodicWorkRequest to send the notification daily
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotificationPublisher.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(notificationWork);
    }
}
