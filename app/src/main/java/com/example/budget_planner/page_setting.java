package com.example.budget_planner;

import android.app.Activity;
import android.app.LauncherActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class page_setting extends BaseActivity {
    private static final String CHANNEL_ID = "export_notification_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREF_SELECTED_ITEM_ID = "selectedItemId";
    private SharedPreferences sharedPreferences;



   FloatingActionButton fab_theme;
    TextView theme_txt;
    RelativeLayout other_theme,budget_duration;

    DBHelper dbHelper;
    private int selectedItemId;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedThemeId = prefs.getInt(SELECTED_THEME_PREF, R.style.Base_Theme_Budget_Planner); // Retrieve the selected theme

        // Apply the fetched theme
        setTheme(selectedThemeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_settings);
        FloatingActionButton back = findViewById(R.id.Back);
        RelativeLayout export = findViewById(R.id.export);
        Spinner dropdownFile = findViewById(R.id.dropdown_file);
        RelativeLayout notification = findViewById(R.id.notification);
        other_theme=findViewById(R.id.other_theme);
        theme_txt=findViewById(R.id.theme_txt);
        budget_duration=findViewById(R.id.Budget_Duration);
        RelativeLayout dark_theme=findViewById(R.id.dark_theme);
        fab_theme=findViewById(R.id.fab_theme);
        dbHelper = new DBHelper(getApplicationContext());

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Set the theme based on the retrieved value
        setTheme(selectedThemeId);

        theme_icon_name();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.add("Transactions");
        adapter.add("Category");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownFile.setAdapter(adapter);
        dropdownFile.setSelection(adapter.getPosition("Transactions"));


        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        selectedItemId = sharedPreferences.getInt(PREF_SELECTED_ITEM_ID, 0);

        if (selectedItemId == -1) {
            // If no item is selected yet, you can set a default item to be checked.
            // For example, check the first item in the menu.
            selectedItemId = R.id.day; // Assuming R.id.menu_item_0 represents the first item in your menu
            // Save selectedItemId to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_SELECTED_ITEM_ID, selectedItemId);
            editor.apply();
        }



        other_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        budget_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                budget_duration_popup(v);
            }
        });


        dark_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(page_setting.this, "Dark mode is only available for Default Theme", Toast.LENGTH_SHORT).show();
                }

            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = dropdownFile.getSelectedItem().toString();
                String fileName = "";
                if (selectedItem.equals("Transactions")) {
                    fileName = "transaction_data.csv";
                } else if (selectedItem.equals("Category")) {
                    fileName = "category_data.csv";
                }
                new ExportDataAsyncTask(fileName).execute(selectedItem, fileName);
            }
        });

    }
    private void budget_duration_popup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.budget_duration_menu, popupMenu.getMenu());
            // Retrieve selected item ID from SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        selectedItemId = sharedPreferences.getInt(PREF_SELECTED_ITEM_ID, -1);
        // Set checked state of radio buttons based on selectedItemId
        if (selectedItemId != -1) {
            MenuItem item = popupMenu.getMenu().findItem(selectedItemId);
            if (item != null) {
                item.setChecked(true);
            }
        }else {
            // If no item is selected yet, you can set a default item to be checked.
            // For example, check the first item in the menu.
            MenuItem firstItem = popupMenu.getMenu().getItem(0);
            if (firstItem != null) {
                firstItem.setChecked(true);
                selectedItemId = firstItem.getItemId();
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Update selectedItemId and highlight the selected radio button
                selectedItemId = item.getItemId();
                item.setChecked(true);

                // Save selectedItemId to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_SELECTED_ITEM_ID, selectedItemId);
                editor.apply();
                return true;
            }
        });

        popupMenu.setGravity(Gravity.END);
        popupMenu.show();
    }



    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.theme_menu, popupMenu.getMenu());

        // Check the selected item based on the current theme
        MenuItem selectedItem = null;
        if (selectedThemeId == R.style.Base_Theme_Budget_Planner) {
            selectedItem = popupMenu.getMenu().findItem(R.id.default_theme);
        } else if (selectedThemeId == R.style.AppTheme_theme2) {
            selectedItem = popupMenu.getMenu().findItem(R.id.theme_2);
        }else if (selectedThemeId == R.style.AppTheme_theme3) {
            selectedItem = popupMenu.getMenu().findItem(R.id.theme_3);
        }else if (selectedThemeId == R.style.AppTheme_theme4) {
            selectedItem = popupMenu.getMenu().findItem(R.id.theme_4);
        }else if (selectedThemeId == R.style.AppTheme_theme5) {
            selectedItem = popupMenu.getMenu().findItem(R.id.theme_5);
        }
        // Add more else if conditions for other themes if needed
        if (selectedItem != null) {
            selectedItem.setChecked(true);
        }

        // Disable other themes if dark mode is enabled
        if (isDarkMode) {
            // Show toast message informing users about theme availability
            Toast.makeText(page_setting.this, "Other themes are only available in light mode", Toast.LENGTH_SHORT).show();
            // Disable other themes
            MenuItem theme2Item = popupMenu.getMenu().findItem(R.id.theme_2);
            MenuItem theme3Item = popupMenu.getMenu().findItem(R.id.theme_3);
            MenuItem theme4Item = popupMenu.getMenu().findItem(R.id.theme_4);
            MenuItem theme5Item = popupMenu.getMenu().findItem(R.id.theme_5);
            // Disable other themes
            theme2Item.setEnabled(false);
            theme3Item.setEnabled(false);
            theme4Item.setEnabled(false);
            theme5Item.setEnabled(false);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int themeResourceId = 0;
                if (item.getItemId() == R.id.default_theme) {
                    // Apply default theme
                    themeResourceId = R.style.Base_Theme_Budget_Planner;
                    showRestartDialog(themeResourceId);
                } else if (item.getItemId() == R.id.theme_2) {
                    // Apply Theme 2
                    themeResourceId = R.style.AppTheme_theme2;
                    showRestartDialog(themeResourceId);
                }else if (item.getItemId() == R.id.theme_3) {
                    // Apply Theme 3
                    themeResourceId = R.style.AppTheme_theme3;
                    showRestartDialog(themeResourceId);
                }else if (item.getItemId() == R.id.theme_4) {
                    // Apply Theme 4
                    themeResourceId = R.style.AppTheme_theme4;
                    showRestartDialog(themeResourceId);
                }else if (item.getItemId() == R.id.theme_5) {
                    // Apply Theme 5
                    themeResourceId = R.style.AppTheme_theme5;
                    showRestartDialog(themeResourceId);
                }
                // Check the selected item
                item.setChecked(true);
                return true;
            }
        });
        popupMenu.setGravity(Gravity.END);
        popupMenu.show();
    }

    private void showRestartDialog(final int themeResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Restart Required");
        builder.setMessage("Changing the theme requires restarting the app. Do you want to continue?");
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Apply the selected theme
                setSelectedTheme(themeResourceId);
                setTheme(themeResourceId);
                // Restart the MainActivity
                Intent intent = new Intent(page_setting.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish the current activity


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User cancelled, do nothing
            }
        });
        builder.show();
    }






    private void theme_icon_name() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Dark theme
            fab_theme.setImageResource(R.drawable.sun_svgrepo_com);
            theme_txt.setText("Light Theme");
        } else {
            // Light theme
            fab_theme.setImageResource(R.drawable.moon_svgrepo_com);
            theme_txt.setText("Dark Theme");
        }
    }



    private class ExportDataAsyncTask extends AsyncTask<String, Void, File> {
        private NotificationManagerCompat notificationManager;
        private NotificationCompat.Builder notificationBuilder;
        private int progressMax;
        private int progress;
        private String fileName;

        public ExportDataAsyncTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notificationManager = NotificationManagerCompat.from(page_setting.this);
            createNotificationChannel();
            notificationBuilder = new NotificationCompat.Builder(page_setting.this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle("Exporting Data")
                    .setContentText("Export in progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setColor(Color.BLUE)
                    .setProgress(0, 0, true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(page_setting.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                } else {
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }
            } else {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        }

        @Override
        protected File doInBackground(String... params) {
            String tableName = params[0];
            String fileName = params[1];
            String query = "SELECT * FROM " + tableName;
            Cursor cursor = dbHelper.readAllData(query);
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            List<String[]> data = new ArrayList<>();
            String[] headerRow;
            if (tableName.equals("Transactions")) {
                headerRow = new String[]{"Transaction ID", "Amount", "Date", "Time", "Category ID", "Note"};
            } else if (tableName.equals("Category")) {
                headerRow = new String[]{"Category ID", "Category Name", "Category Icon", "Type"};
            } else {
                return null;
            }
            data.add(headerRow);

            while (cursor.moveToNext()) {
                String[] rowData = new String[cursor.getColumnCount()];
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    rowData[i] = cursor.getString(i);
                }
                data.add(rowData);
            }
            cursor.close();

            return createCsvFile(data, fileName);
        }


        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            notificationBuilder.setProgress(0, 0, false)
                    .setOngoing(false);
            if (result != null) {
                notificationBuilder.setContentTitle("Export Completed")
                        .setContentText("Data exported successfully. File Saved as " + fileName+" in Downloads")
                        .setSmallIcon(R.drawable.tick_svgrepo_com);
            } else {
                notificationBuilder.setContentTitle("Export Failed")
                        .setContentText("Failed to export data")
                        .setSmallIcon(android.R.drawable.stat_notify_error);
            }

            if (ActivityCompat.checkSelfPermission(page_setting.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Handle permission request if needed
            } else {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        }



        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Export Notification";
                String description = "Notification for export progress";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }

    private File createCsvFile(List<String[]> data, String fileName) {
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDirectory, fileName);

        try {
            FileWriter writer = new FileWriter(csvFile);

            for (String[] rowData : data) {
                for (int i = 0; i < rowData.length; i++) {
                    writer.append(escapeSpecialCharacters(rowData[i]));
                    if (i != rowData.length - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }

            writer.flush();
            writer.close();
            return csvFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String escapeSpecialCharacters(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        } else {
            return value;
        }
    }
}
