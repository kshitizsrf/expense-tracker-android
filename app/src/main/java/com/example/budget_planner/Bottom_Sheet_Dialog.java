package com.example.budget_planner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Bottom_Sheet_Dialog extends BottomSheetDialogFragment {

    private TextView expensesTab;
    private TextView incomeTab;
    private Fragment selectedFragment; // Track the selected tab

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false);

        // Apply theme
        SharedPreferences prefs = requireContext().getSharedPreferences(BaseActivity.PREFS_NAME, requireContext().MODE_PRIVATE);
        int selectedThemeId = prefs.getInt(BaseActivity.SELECTED_THEME_PREF, R.style.Base_Theme_Budget_Planner);
        getContext().getTheme().applyStyle(selectedThemeId, true);

        // Find views
        expensesTab = view.findViewById(R.id.expenses_cat);
        incomeTab = view.findViewById(R.id.income_cat);

        // Set initial background color for tabs
        expensesTab.setBackgroundResource(R.drawable.round_shape_for_tabs);
        incomeTab.setBackgroundResource(R.drawable.round_back_for_tabs);

        // Set click listeners for tabs
        expensesTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(new Expense_Sheet_Fragment(), expensesTab, incomeTab);
            }
        });

        incomeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(new Income_Sheet_Fragment(), incomeTab, expensesTab);
            }
        });

        // Initially, set the Expenses tab as selected
        switchTab(new Expense_Sheet_Fragment(), expensesTab, incomeTab);

        return view;
    }

    private void switchTab(Fragment fragment, TextView selectedTab, TextView unselectedTab) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(R.id.fragmentcontainer, fragment);
        fragmentTransaction.commit();

        // Reset the background color for tabs
        selectedTab.setBackgroundResource(R.drawable.round_shape_for_tabs);
        unselectedTab.setBackgroundResource(R.drawable.round_back_for_tabs);

        // Update the selected fragment
        selectedFragment = fragment;
    }
}
