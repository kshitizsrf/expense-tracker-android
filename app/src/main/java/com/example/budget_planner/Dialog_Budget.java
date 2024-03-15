package com.example.budget_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Dialog_Budget extends AppCompatActivity {

    private EditText editTextBudget;
    TextView selectedDate;
    Button datePicker;
    String mode;
    String inputbudget;
    String dateRange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_dialog);

        // Initialize views
        editTextBudget = findViewById(R.id.txt_budget);
        Button set_btn = findViewById(R.id.set_btn);
        Button cancel_Button=findViewById(R.id.cancel_button);
        FloatingActionButton back = findViewById(R.id.Back);
        selectedDate = findViewById(R.id.selectedDate);
        mode = getIntent().getStringExtra("mode");
        if ("edit".equals(mode)) {
            String budget = getIntent().getStringExtra("budget");
            String startDate = getIntent().getStringExtra("startDate");
            String endDate = getIntent().getStringExtra("endDate");
            if("Not Set".equals(budget)) {
                editTextBudget.setText("");
                selectedDate.setText("");
            }else{
                editTextBudget.setText(budget);
                selectedDate.setText(startDate + " - " + endDate);
            }
        }

        editTextBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    cancel_Button.setText("Clear");
                } else {
                    cancel_Button.setText("Cancel");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (!TextUtils.isEmpty(editTextBudget.getText().toString().trim())) {
            cancel_Button.setText("Clear");
        }
        // Setting click listener for the date picker button
        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerdialog();
            }
        });

        cancel_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(editTextBudget.getText().toString().trim()) ||
                        !TextUtils.isEmpty(selectedDate.getText().toString().trim())
                ){
                    editTextBudget.setText("");
                    selectedDate.setText("");
                } else {

                    finish();
                }
            }
        });

        set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputbudget = editTextBudget.getText().toString().trim();
                dateRange = selectedDate.getText().toString().trim();
                if (TextUtils.isEmpty(inputbudget) && TextUtils.isEmpty(dateRange)) {
                    inputbudget = "Not Set";
                    String startDate = "";
                    String endDate = "";

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("budget", inputbudget);
                    resultIntent.putExtra("startDate", startDate);
                    resultIntent.putExtra("endDate", endDate);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    finish();
                    return; // Finish this method call
                }
                String[] dates = dateRange.split(" - ");
                String startDate = dates[0];
                String endDate = dates[1];
                // Increment the end date by one day
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date endDateFormat = sdf.parse(endDate);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(endDateFormat);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    endDate = sdf.format(calendar.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if(!TextUtils.isEmpty(editTextBudget.getText().toString().trim())){
                    if(TextUtils.isEmpty(selectedDate.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(),"Select your Date",Toast.LENGTH_SHORT).show();
                    }else {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("budget", inputbudget);
                        resultIntent.putExtra("startDate", startDate);
                        resultIntent.putExtra("endDate", endDate);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("budget", inputbudget);
                    resultIntent.putExtra("startDate", startDate);
                    resultIntent.putExtra("endDate", endDate);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void DatePickerdialog() {
        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a date range");
        builder.setTheme(R.style.CustomMaterialCalendar);
        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Retrieving the selected start and end dates
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Formating the selected dates as strings
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));

            // Creating the date range string
            String selectedDateRange = startDateString + " - " + endDateString;

            // Displaying the selected date range in the TextView
            selectedDate.setText(selectedDateRange);
        });

        // Showing the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}
