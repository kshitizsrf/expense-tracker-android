package com.example.budget_planner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.button.MaterialButton;

public class std_bottom_sheet extends BaseActivity {
    AppCompatButton button1,button2,button3,button4,button5,button6,button7,button8,button9,button0,button_div,button_multiply,button_add,button_sub,button_c,button_dot;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedThemeId = prefs.getInt(SELECTED_THEME_PREF, R.style.Base_Theme_Budget_Planner); // Retrieve the selected theme

        // Apply the fetched theme
        setTheme(selectedThemeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_bottom_sheet);




    }


}
