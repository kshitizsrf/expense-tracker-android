package com.example.budget_planner;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chart_Fragment extends Fragment {

    PieChart expenseChart;
    PieChart incomeChart;

    List<PieEntry> expenseEntries;
    List<PieEntry> incomeEntries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_, container, false);

        expenseChart = view.findViewById(R.id.expense_chart);
        incomeChart = view.findViewById(R.id.income_chart);

        expenseEntries = new ArrayList<>();
        incomeEntries = new ArrayList<>();

        setExpenseValues();
        setIncomeValues();

        setUpChart(expenseChart, expenseEntries, "Expense");
        setUpChart(incomeChart, incomeEntries, "Income");

        return view;
    }

    private void setUpChart(PieChart chart, List<PieEntry> entries, String chartLabel) {
        PieDataSet pieDataSet = new PieDataSet(entries, chartLabel);
        PieData pieData = new PieData(pieDataSet);
        int[] chartColors = ColorTemplate.COLORFUL_COLORS;
        pieDataSet.setColors(chartColors);

        // Set label text color and value text color to black
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextColors(Collections.singletonList(Color.BLACK));
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
        chart.invalidate();
    }


    private void setExpenseValues() {
        expenseEntries.add(new PieEntry(200, "Food"));
        expenseEntries.add(new PieEntry(100, "clothes"));
        // Add more entries if needed
    }

    private void setIncomeValues() {
        incomeEntries.add(new PieEntry(300, "Salary"));
        // Add more entries if needed
    }
}
