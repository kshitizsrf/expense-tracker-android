package com.example.budget_planner;// Bottom_Sheet_Dialog.java

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Bottom_Sheet_Dialog extends AppCompatActivity {

    private TextView expensesTab;
    private TextView incomeTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet_dialog);

        expensesTab = findViewById(R.id.expenses_cat);
        incomeTab = findViewById(R.id.income_cat);

        expensesTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to Expenses tab
                switchTab(new Expense_Sheet_Fragment());
            }
        });

        incomeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to Income tab
                switchTab(new Income_Sheet_Fragment());
            }
        });

        // Initially, set the Expenses tab as selected
        switchTab(new Expense_Sheet_Fragment());
    }

    private void switchTab(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentcontainer, fragment);
        transaction.addToBackStack(null); // Add this line to enable back navigation
        transaction.commit();
    }
}
