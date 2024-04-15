package com.example.budget_planner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Home_Fragment extends Fragment {
    private DBHelper mydb;
    private static final String PREF_BUDGET_KEY = "budget";
    private static final String PREF_START_DATE_KEY = "start_date";
    private static final String PREF_END_DATE_KEY = "end_date";

    private SimpleDateFormat sdfDatabase = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    TextView month_txt,balance_amount,exp_total,income_total,remaining_budget;
    FloatingActionButton next,back;
    TextView empty_txt;
    String startDateString,endDateString;
    private ArrayList<Double> amount;
    private ArrayList<Integer> id;
    private ArrayList<String> time;
    private ArrayList<String> Category_id;
    private ArrayList<String> note;
    private ArrayList<String> date;
    private TransactionAdapter adapter;
    ProgressBar progress_budget;
    private Calendar calendar;
    TextView show_budget;
    Dialog_Budget dialogBudget;
    //private String query = "SELECT *, strftime('%Y-%m-%d', date) AS sortable_date FROM Transactions ORDER BY sortable_date DESC LIMIT 6";
    private String query = "SELECT *, "
            + "substr(date, -4) || '-' || "
            + "CASE substr(date, 1, 3) "
            + "    WHEN 'Jan' THEN '01' "
            + "    WHEN 'Feb' THEN '02' "
            + "    WHEN 'Mar' THEN '03' "
            + "    WHEN 'Apr' THEN '04' "
            + "    WHEN 'May' THEN '05' "
            + "    WHEN 'Jun' THEN '06' "
            + "    WHEN 'Jul' THEN '07' "
            + "    WHEN 'Aug' THEN '08' "
            + "    WHEN 'Sep' THEN '09' "
            + "    WHEN 'Oct' THEN '10' "
            + "    WHEN 'Nov' THEN '11' "
            + "    WHEN 'Dec' THEN '12' "
            + "END || '-' || "
            + "substr(date, 5, 2) || ' ' || "
            + "substr(date, -8) AS sortable_date "
            + "FROM Transactions ORDER BY sortable_date DESC LIMIT 6";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_, container, false);
        FloatingActionButton add_fab = view.findViewById(R.id.add);
        TextView budget = view.findViewById(R.id.clickableText);
        month_txt=view.findViewById(R.id.months);
        next=view.findViewById(R.id.next);
        back=view.findViewById(R.id.back);
        show_budget = view.findViewById(R.id.show_budget);
        TextView see_all=view.findViewById(R.id.seeAll);
        ListView listView=view.findViewById(R.id.list_view);
        balance_amount=view.findViewById(R.id.balance_amount);
        empty_txt=view.findViewById(R.id.empty);
        exp_total=view.findViewById(R.id.exp_total);
        income_total=view.findViewById(R.id.income_total);
        remaining_budget=view.findViewById(R.id.Remaining_budget);
        progress_budget=view.findViewById(R.id.progress_budget);
        calendar = Calendar.getInstance();
        show_budget.setText("Not Set");
        // Retrieve selectedthemeId from arguments
        int selectedThemeId = getArguments().getInt("selectedThemeId", -1);

        // Check if the theme is AppTheme_theme3
        if (selectedThemeId == R.style.AppTheme_theme3) {
            // Change text color for AppTheme_theme3
            show_budget.setTextColor(Color.parseColor("#939393"));
            remaining_budget.setTextColor(Color.parseColor("#939393"));
        }



        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >0) {
                    // Hide the add button if the user has scrolled down
                    add_fab.hide();
                } else {
                    // Show the add button if the user is at the top of the list
                    add_fab.show();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1); // Move to the next month
                YoYo.with(Techniques.FadeInRight).duration(800).repeat(0).playOn(month_txt);
                YoYo.with(Techniques.FadeInRight).duration(800).repeat(0).playOn(balance_amount);
                YoYo.with(Techniques.FadeInRight).duration(800).repeat(0).playOn(exp_total);
                YoYo.with(Techniques.FadeInRight).duration(800).repeat(0).playOn(income_total);
                updateMonthAndYear();
                updateBalanceAmount();

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1); // Move to the previous month
                YoYo.with(Techniques.FadeInLeft).duration(800).repeat(0).playOn(month_txt);
                YoYo.with(Techniques.FadeInLeft).duration(800).repeat(0).playOn(balance_amount);
                YoYo.with(Techniques.FadeInLeft).duration(800).repeat(0).playOn(exp_total);
                YoYo.with(Techniques.FadeInLeft).duration(800).repeat(0).playOn(income_total);
                updateMonthAndYear();
                updateBalanceAmount();

                
            }
        });

        see_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.replaceFragment(new Transaction_Fragment());
                mainActivity.updateTitle(MainActivity.ID_TRANSACTIONS);
                mainActivity.updateBottomNavigation(MainActivity.ID_TRANSACTIONS);
            }
        });
        show_budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(show_budget.getText().toString().trim())) {
                    Intent intent = new Intent(getActivity(), Dialog_Budget.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("budget", show_budget.getText().toString().trim());
                    intent.putExtra("startDate",startDateString);
                    intent.putExtra("endDate", endDateString);
                    startActivityForResult(intent, 1);

                }else {
                    Intent intent = new Intent(getActivity(), Dialog_Budget.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

        budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(show_budget.getText().toString().trim())) {
                    Intent intent = new Intent(getActivity(), Dialog_Budget.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("budget", show_budget.getText().toString().trim());
                    intent.putExtra("startDate",startDateString);
                    intent.putExtra("endDate", endDateString);
                    startActivityForResult(intent, 1);

                }else {
                    Intent intent = new Intent(getActivity(), Dialog_Budget.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming you have the 'page_add' activity declared in your manifest
                Intent intent = new Intent(getActivity(), page_add.class);
                startActivity(intent);
            }
        });
        mydb = new DBHelper(getActivity());
        id=new ArrayList<>();
        amount = new ArrayList<>();
        time = new ArrayList<>();
        Category_id = new ArrayList<>();
        note = new ArrayList<>();
        date = new ArrayList<>();
        adapter = new TransactionAdapter(getActivity(),id, amount, time, Category_id, note, date);
        listView.setAdapter(adapter);
        updateMonthAndYear();
        updateBalanceAmount();
        adjustTextSize(income_total);
        adjustTextSize(exp_total);
        // Fetch data from database
        storeDataInArray();
        adjustTextSize(income_total);
        adjustTextSize(exp_total);
        updateRemainingBudgetAmount();
        return view;
    }

    private void adjustTextSize(TextView textView) {
        String text = textView.getText().toString();
        int length = text.length();

        // Define your logic for adjusting text size here
        // For example, you can set different text sizes based on the length of the text
        if (length >8) {
            textView.setTextSize(18); // Set a larger text size
        } else if (length>11) {
            textView.setTextSize(20);
        } else {
            textView.setTextSize(25); // Set a smaller text size
        }
    }
    private void updateBalanceAmount() {
        // Fetch and update the balance amount for the displayed month
        String displayedMonth = month_txt.getText().toString();
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        try {
            Date displayedDate = sdfDisplay.parse(displayedMonth);
            String startOfMonth = getStartOfMonth(displayedDate);
            String endOfMonth = getEndOfMonth(displayedDate);

            // Construct the query to filter transactions for the displayed month
            String query = "SELECT * FROM Transactions WHERE date BETWEEN '" + startOfMonth + "' AND '" + endOfMonth + "'";
            Cursor transactionCursor = mydb.readAllData(query);

            double totalExpense = 0;
            double totalIncome = 0;

            while (transactionCursor.moveToNext()) {
                int column_amount=transactionCursor.getColumnIndex("amount");
                int column_cat_id=transactionCursor.getColumnIndex("category_id");
                double amount = transactionCursor.getDouble(column_amount);
                int categoryId = transactionCursor.getInt(column_cat_id);
                String type = mydb.getCategoryType(categoryId);

                if ("Expense".equals(type)) {
                    totalExpense += amount;
                } else if ("Income".equals(type)) {
                    totalIncome += amount;
                }
            }

            // Update UI with total expense and income
            balance_amount.setText(String.format(Locale.getDefault(), "%.2f", totalIncome - totalExpense));
            exp_total.setText(String.format(Locale.getDefault(), "%.2f",  totalExpense));
            income_total.setText(String.format(Locale.getDefault(), "%.2f", totalIncome ));
            transactionCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void updateRemainingBudgetAmount() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String budget=sharedPreferences.getString(PREF_BUDGET_KEY,"");
        startDateString = sharedPreferences.getString(PREF_START_DATE_KEY, "");
        endDateString = sharedPreferences.getString(PREF_END_DATE_KEY, "");

        // Convert SharedPreferences date format to match the database date format
        SimpleDateFormat sharedPrefsSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbSdf = new SimpleDateFormat("MMM dd,yyyy", Locale.getDefault());
        try {
            Date startDate = sharedPrefsSdf.parse(startDateString);
            Date endDate = sharedPrefsSdf.parse(endDateString);

            // Subtract one day from the endDate
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            endDate = calendar.getTime();

            String start = dbSdf.format(startDate);
            String end = dbSdf.format(endDate);

            // Construct the query to filter transactions for the specified date range
            String query = "SELECT * FROM Transactions WHERE date BETWEEN '" + start + "' AND '" + end + "'";
            Cursor transactionCursor = mydb.readAllData(query);
            double rangeSumExpense = 0; // Variable to store sum of expense transactions within the specified date range

            while (transactionCursor.moveToNext()) {
                int columnAmount = transactionCursor.getColumnIndex("amount");
                int columnCategoryId = transactionCursor.getColumnIndex("category_id");
                double amount = transactionCursor.getDouble(columnAmount);
                int categoryId = transactionCursor.getInt(columnCategoryId);
                String type = mydb.getCategoryType(categoryId);

                // Check if the transaction is an expense
                if ("Expense".equals(type)) {
                    // Increment rangeSumExpense only for transactions within the specified date range
                    rangeSumExpense += amount;
                }
            }


            // Set the remaining budget text outside the while loop
            if(!"Not Set".equals(show_budget.getText().toString().trim())) {
                int remaining= (int) (Integer.parseInt(budget)-rangeSumExpense);
                String remaining_String=String.valueOf(remaining);
                remaining_budget.setText(remaining_String);

                progress_budget.setMax(Integer.parseInt(budget));
                progress_budget.setProgress((int) rangeSumExpense);
                progress_budget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2DC503")));
            }

            if ((int) rangeSumExpense>=Integer.parseInt(budget)){
                progress_budget.setProgressTintList(ColorStateList.valueOf(Color.RED));
            }
            // Close the cursor outside the try-catch block
            transactionCursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the parsing exception according to your application logic
        }
    }




    private String getStartOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();
        return sdfDatabase.format(startDate);
    }

    private String getEndOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();
        return sdfDatabase.format(endDate);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 &&resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String budget = data.getStringExtra("budget");
                String startDate = data.getStringExtra("startDate");
                String endDate = data.getStringExtra("endDate");

                if (budget != null && startDate != null && endDate != null) {
                    // Update UI with budget, start date, and end date
                    show_budget.setText(budget);
                    // Handle start date and end date as needed
                    // For example, you can parse them into Date objects or display them in TextViews
                    saveBudgetAndDates(budget, startDate, endDate);
                }
            }
        }
    }

    private void saveBudgetAndDates(String budget, String startDate, String endDate) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_BUDGET_KEY, budget);
        editor.putString(PREF_START_DATE_KEY, startDate);
        editor.putString(PREF_END_DATE_KEY, endDate);
        editor.apply();
    }

    private void retrieveBudgetAndDates() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String budget = sharedPreferences.getString(PREF_BUDGET_KEY, null);
        String startDate = sharedPreferences.getString(PREF_START_DATE_KEY, null);
        String endDate = sharedPreferences.getString(PREF_END_DATE_KEY, null);

        if (budget != null && startDate != null && endDate != null) {
            // Parse start date and end date into Date objects
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);



                // Get current date
                Calendar calendar = Calendar.getInstance();
                Date currentDate = calendar.getTime();

                // Check if current date is within the range of start and end dates
                if ((currentDate.after(start) && currentDate.before(end))) {
                    // Update UI with budget
                    show_budget.setText(budget);
                    return;
                } else {
                    // Current date is not within the range, show empty value
                    show_budget.setText("Not Set");
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }







    private void updateMonthAndYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthAndYear = sdf.format(calendar.getTime());
        month_txt.setText(monthAndYear);
    }
    public void onResume() {
        super.onResume();
        // Fetch data from database
        updateBalanceAmount();
        storeDataInArray();
        retrieveBudgetAndDates();
        updateRemainingBudgetAmount();
    }
    public void storeDataInArray() {
        id.clear();
        amount.clear();
        time.clear();
        Category_id.clear();
        note.clear();
        date.clear();
        Cursor cursor = mydb.readAllData(query);
        if (cursor.getCount() == 0) {
            empty_txt.setVisibility(View.VISIBLE);
        } else {
            empty_txt.setVisibility(View.GONE);
            while (cursor.moveToNext()) {
                int column_id = cursor.getColumnIndex("transaction_id");
                int column_amount = cursor.getColumnIndex("amount");
                int column_time = cursor.getColumnIndex("time");
                int column_cat_id = cursor.getColumnIndex("category_id");
                int column_note = cursor.getColumnIndex("note");
                int column_date = cursor.getColumnIndex("date");
                id.add(cursor.getInt(column_id));
                amount.add(cursor.getDouble(column_amount));
                time.add(cursor.getString(column_time));
                Category_id.add(cursor.getString(column_cat_id));
                note.add(cursor.getString(column_note));
                date.add(cursor.getString(column_date));
            }
            // Notify adapter about the data change
            adapter.notifyDataSetChanged();
        }
        cursor.close();
    }


}
