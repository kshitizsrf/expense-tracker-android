package com.example.budget_planner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Home_Fragment extends Fragment {

    public void openDialog(View view) {
        Dialog_Budget dialogBudget = new Dialog_Budget();
        dialogBudget.setTargetFragment(this, 0);
        dialogBudget.show(getActivity().getSupportFragmentManager(), "Test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_, container, false);
        FloatingActionButton add_fab = view.findViewById(R.id.add);
        TextView budget = view.findViewById(R.id.clickableText);
        TextView show_budget = view.findViewById(R.id.budget);
        TextView see_all=view.findViewById(R.id.seeAll);
        show_budget.setText("0000");

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
                openDialog(view);
            }
        });

        budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(view);
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

        return view;
    }
}
