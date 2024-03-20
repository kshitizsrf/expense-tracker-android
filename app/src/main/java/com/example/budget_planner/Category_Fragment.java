package com.example.budget_planner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Category_Fragment extends Fragment {

    private TextView exp_cat, inc_cat;
    private int selectedTabNum = 1;
    public static String category="Expense";
    FloatingActionButton add_cat;
    private static final int ANIMATION_DURATION = 500; // Set your desired duration here

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_, container, false);

        exp_cat = view.findViewById(R.id.expenses_cat);
        inc_cat = view.findViewById(R.id.income_cat);
        add_cat=view.findViewById(R.id.add_cat);
        add_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),Category_Settings.class);
                startActivity(intent);
            }
        });

        initializeFragments();

        exp_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(new Expenses_Category_Fragment());
            }
        });

        inc_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(new Income_Category_Fragment());
            }
        });

        return view;
    }

    public void setFabVisibility(boolean isVisible) {

        if (isVisible) {
            add_cat.show();
        } else {
            add_cat.hide();
        }
    }

    // This method will be called from the child fragment to notify about list scroll
    public void onListScroll(boolean isScrolled) {
        setFabVisibility(!isScrolled);
    }


    private void initializeFragments() {
        replaceFragment(new Expenses_Category_Fragment());
    }

    private void selectTab(Fragment fragment) {
        TextView selectTextView, nonSelectTV;


        if (fragment instanceof Expenses_Category_Fragment) {
            selectTextView = exp_cat;
            nonSelectTV = inc_cat;
            category = "Expense";
        } else {
            selectTextView = inc_cat;
            nonSelectTV = exp_cat;
            category = "Income";
        }

        float slideTo = 0;

        if (fragment instanceof Expenses_Category_Fragment) {
            slideTo = selectTextView.getWidth();
        } else {
            slideTo = -selectTextView.getWidth();
        }

        TranslateAnimation translateAnimation = new TranslateAnimation(0, slideTo, 0, 0);
        translateAnimation.setDuration(ANIMATION_DURATION);

        applyAnimation(selectTextView, nonSelectTV, translateAnimation);

        replaceFragment(fragment);
    }



    private void applyAnimation(TextView selectTextView, TextView nonSelectTV, TranslateAnimation translateAnimation) {
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Handle animation start if needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                selectTextView.setBackgroundResource(R.drawable.round_shape_for_tabs);
                nonSelectTV.setBackgroundColor(Color.TRANSPARENT);
                selectTextView.setTextColor(Color.BLACK);
                nonSelectTV.setTextColor(Color.parseColor("#80000000"));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Handle animation repeat if needed
            }
        });

        selectTextView.startAnimation(translateAnimation);
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(
                        R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.fragmentcontainer, fragment, null)
                .commit();
    }
}
