package com.example.budget_planner;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class Chart_Fragment extends Fragment {

    PieChart expenseChart;
    ListView listView_Expense,listView_Income;
    PieChart incomeChart;

    List<PieEntry> expenseEntries;
    List<PieEntry> incomeEntries;

    DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_, container, false);

        expenseChart = view.findViewById(R.id.expense_chart);
        incomeChart = view.findViewById(R.id.income_chart);
        listView_Expense = view.findViewById(R.id.listview_Expense);
        listView_Income = view.findViewById(R.id.listview_income);
        expenseEntries = new ArrayList<>();
        incomeEntries = new ArrayList<>();

        dbHelper = new DBHelper(getContext());

        // Fetch data from database and populate the entries
        populateExpenseEntries();
        populateIncomeEntries();

        // Set up charts with populated entries
        setUpChart(expenseChart, expenseEntries, "Expense");
        setUpChart(incomeChart, incomeEntries, "Income");

        animateCharts();

        return view;
    }

    private void animateCharts() {
        // Animate expense chart
        expenseChart.animateY(1400, Easing.EaseInOutQuad);

        // Animate income chart
        incomeChart.animateY(1400, Easing.EaseInOutQuad);
    }

    private void setUpChart(PieChart chart, List<PieEntry> entries, String chartLabel) {
        PieDataSet pieDataSet = new PieDataSet(entries, chartLabel);
        PieData pieData = new PieData(pieDataSet);
        int[] chartColors = ColorTemplate.COLORFUL_COLORS;
        pieDataSet.setColors(chartColors);

        // Set label text color and value text color to black
        pieDataSet.setValueTextColor(Color.WHITE);
        chart.setCenterText(chartLabel);
        chart.setCenterTextSize(16f);
        pieData.setValueTextSize(12f);

        chart.setHoleRadius(92);
        chart.setHoleColor(Color.parseColor("#F2F2F2"));
        chart.setData(pieData);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Custom offset for labels
        pieDataSet.setValueLinePart1OffsetPercentage(50f);
        pieDataSet.setValueLinePart1OffsetPercentage(50f);
        pieDataSet.setValueLineColor(Color.WHITE);
        chart.invalidate();
    }

    private void populateExpenseEntries() {
        Cursor cursor = dbHelper.realAllData("SELECT c.category_icon,c.category_name, SUM(t.amount) AS total_amount FROM Transactions t INNER JOIN Category c ON t.category_id = c.category_id WHERE c.type='Expense' GROUP BY c.category_name");
        List<String> categoryNames = new ArrayList<>();
        List<String> amount = new ArrayList<>();
        List<String> categoryicon = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int caticon=cursor.getColumnIndex("category_icon");
                int catname=cursor.getColumnIndex("category_name");
                int tt=cursor.getColumnIndex("total_amount");
                String icon = cursor.getString(caticon);
                String categoryName = cursor.getString(catname);
                categoryNames.add(categoryName);
                float totalAmount = cursor.getFloat(tt);
                amount.add(String.valueOf(totalAmount));
                categoryicon.add(icon);
                expenseEntries.add(new PieEntry(totalAmount, categoryName));
            } while (cursor.moveToNext());
            cursor.close();
        }
        ChartAdapter adapter = new ChartAdapter(requireContext(), categoryicon,categoryNames,amount);
        listView_Expense.setAdapter(adapter);
    }

    private void populateIncomeEntries() {
        Cursor cursor = dbHelper.realAllData("SELECT c.category_icon, c.category_name, SUM(t.amount) AS total_amount FROM Transactions t INNER JOIN Category c ON t.category_id = c.category_id WHERE c.type='Income' GROUP BY c.category_icon, c.category_name");
        List<String> categoryNames = new ArrayList<>();
        List<String> amount = new ArrayList<>();
        List<String> categoryIcon = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int caticon = cursor.getColumnIndex("category_icon");
                int catname = cursor.getColumnIndex("category_name");
                int tt = cursor.getColumnIndex("total_amount");
                String icon = cursor.getString(caticon);
                String categoryName = cursor.getString(catname);
                float totalAmount = cursor.getFloat(tt);
                categoryNames.add(categoryName);
                amount.add(String.valueOf(totalAmount));
                categoryIcon.add(icon);
                incomeEntries.add(new PieEntry(totalAmount, categoryName));
            } while (cursor.moveToNext());
            cursor.close();
        }
        ChartAdapter adapter = new ChartAdapter(requireContext(), categoryIcon, categoryNames, amount);
        listView_Income.setAdapter(adapter);
    }

}
