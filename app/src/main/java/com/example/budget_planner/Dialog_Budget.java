package com.example.budget_planner;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

public class Dialog_Budget extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.budget_dialog, null);

        builder.setView(view);

        // Set title
        builder.setTitle("Set Budget");

        // Set buttons
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText budget = view.findViewById(R.id.txt_budget);
                String inputValue = budget.getText().toString();

                Home_Fragment homeFragment = (Home_Fragment) getTargetFragment();
                if (homeFragment != null) {
                    TextView textView = homeFragment.getView().findViewById(R.id.budget);
                    if (textView != null) {
                        textView.setText(inputValue);
                    } else {
                        // Log if textView is null
                        Log.e("Dialog_Budget", "TextView is null");
                    }
                } else {
                    // Log if homeFragment is null
                    Log.e("Dialog_Budget", "Home_Fragment is null");
                }

                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Set button text color using the custom style
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextColor(Color.parseColor("#800080"));
                negativeButton.setTextColor(Color.parseColor("#800080"));
            }
        });

        // Return the created AlertDialog
        return alertDialog;
    }
}
