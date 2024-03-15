package com.example.budget_planner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class page_add extends AppCompatActivity {
    EditText date;
    private String mode;
    EditText category;
    EditText time;
    TextView amount, cal_amount;
    String calAmountText="";
    EditText note;
    DBHelper dbHelper;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_add);
        FloatingActionButton back = findViewById(R.id.Back);
        showDialog();
        category = findViewById(R.id.category);
        amount = findViewById(R.id.amount);
        cal_amount = findViewById(R.id.cal_amount);
        View bottomSheet = findViewById(R.id.sheet);
        ImageView expand_icon = findViewById(R.id.expand_icon);
        note=findViewById(R.id.note);
        Button add_data=findViewById(R.id.submit);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(100);



        initBottomSheetButtons();
        updateCalAmountVisibility();


        add_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTransactionToDB();
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateCalAmountVisibility();
            }
        });

        amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        expand_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

            // Set current date
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(currentDate.getTime());
            date.setText(formattedDate);


            // Set current time
            int hour = currentDate.get(Calendar.HOUR);
            int minute = currentDate.get(Calendar.MINUTE);
            int am_pm = currentDate.get(Calendar.AM_PM);
            String amPmString = (am_pm == Calendar.AM) ? "AM" : "PM";
            hour = (hour == 0 || hour == 12) ? 12 : hour % 12;
            time.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPmString));

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }
    public void setCategoryName(String categoryName) {
        category.setText(categoryName);
    }

    private void addTransactionToDB() {
        // Ensure dbHelper is initialized before using it
        if (dbHelper == null) {
            dbHelper = new DBHelper(getApplicationContext());
        }

        // Get the value of calAmountText
        String calAmountText = amount.getText().toString().trim();

        // Check if calAmountText is not empty
        if (!TextUtils.isEmpty(calAmountText)) {
            try {
                // Try parsing calAmountText to a double
                double amountValue = Double.parseDouble(calAmountText);

                String dateValue = date.getText().toString(); // Assuming date holds the date value
                String timeValue = time.getText().toString(); // Assuming time holds the time value
                String categoryNameValue = category.getText().toString(); // Assuming category holds the category name value
                String noteValue = note.getText().toString(); // Set your note value here
                if(!TextUtils.isEmpty(noteValue)){
                    long newRowId = dbHelper.addTransaction(amountValue, dateValue, timeValue, categoryNameValue, noteValue, this);
                    finish();
                    // Add the transaction to the database
                    if (newRowId != -1) {
                         // Transaction added successfully
                    } else {
                         // Failed to add transaction
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Note is empty", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                // Handle the case where calAmountText cannot be parsed to a double
                Toast.makeText(getApplicationContext(), "Invalid amount value", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where calAmountText is empty
            Toast.makeText(getApplicationContext(), "Amount value is empty", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateCalAmountVisibility() {
        if (TextUtils.isEmpty(amount.getText().toString().trim())) {
            cal_amount.setVisibility(View.GONE);
            amount.getLayoutParams().height = (int) getResources().getDimensionPixelSize(R.dimen.def_amount_size);
            amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
            calAmountText=cal_amount.getText().toString();
        } else {
            cal_amount.setVisibility(View.VISIBLE);
            amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            amount.getLayoutParams().height = (int) getResources().getDimensionPixelSize(R.dimen.amount_size);
        }
    }

    private void initBottomSheetButtons() {
        assignedId(R.id.button1);
        assignedId(R.id.button2);
        assignedId(R.id.button3);
        assignedId(R.id.button4);
        assignedId(R.id.button5);
        assignedId(R.id.button6);
        assignedId(R.id.button7);
        assignedId(R.id.button8);
        assignedId(R.id.button9);
        assignedId(R.id.button0);
        assignedId(R.id.button_c);
        assignedId(R.id.button_div);
        assignedId(R.id.button_multiply);
        assignedId(R.id.button_sub);
        assignedId(R.id.button_add);
        assignedId(R.id.button_dot);
    }

    private void assignedId(int id) {
        AppCompatButton btn = findViewById(id);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatButton button = (AppCompatButton) v;
                String buttonText = button.getText().toString();
                String data = amount.getText().toString();
                if (buttonText.equals("C")) {
                    if (data.length() > 0) {
                        data = data.substring(0, data.length() - 1);
                    } else {
                        return;
                    }
                } else {
                    data += buttonText;
                }
                if (id == R.id.button_c) {
                    btn.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            amount.setText("");
                            return true;
                        }
                    });
                }
                amount.setText(data);
                String finalresult = getresult(data);
                if (!finalresult.equals("Err")) {
                    cal_amount.setText(finalresult);
                }
            }
        });
    }

    String getresult(String data) {
        try {
            if (data.isEmpty()) {
                return "0";
            }
            data = data.replaceAll("÷", "/");
            data = data.replaceAll("×", "*");
            data = data.replaceAll("−", "-");
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalresult = context.evaluateString(scriptable, data, "Javascript", 1, null).toString();
            if (finalresult.endsWith(".0")) {
                finalresult = finalresult.replace(".0", "");
            }
            return finalresult;
        } catch (Exception e) {
            return "Err";
        }
    }

    private void showDialog() {
        Bottom_Sheet_Dialog bottomSheetDialog = new Bottom_Sheet_Dialog();
        bottomSheetDialog.show(getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int initialYear = calendar.get(Calendar.YEAR);
        int initialMonth = calendar.get(Calendar.MONTH);
        int initialDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = sdf.format(selectedDate.getTime());
                date.setText(formattedDate);
            }
        }, initialYear, initialMonth, initialDay);

        dialog.show();
    }


    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int initialHour = calendar.get(Calendar.HOUR_OF_DAY); // Use HOUR_OF_DAY to get the 24-hour format
        int initialMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String am_pm;
                int hour;
                if (hourOfDay >= 12) {
                    am_pm = "PM";
                    hour = (hourOfDay == 12) ? 12 : hourOfDay - 12;
                } else {
                    am_pm = "AM";
                    hour = (hourOfDay == 0) ? 12 : hourOfDay;
                }
                time.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, am_pm));
            }
        }, initialHour, initialMinute, false);

        dialog.show();
    }

}
