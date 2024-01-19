package com.example.budget_planner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private final int ID_HOME = 1;
    private final int ID_CHART = 2;
    public static final int ID_TRANSACTIONS = 3;
    private final int ID_CATEGORY = 4;

    private Toolbar toolbar;

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void updateTitle(int itemId) {
        String title = "";
        switch (itemId) {
            case ID_HOME:
                title = "Home";
                break;
            case ID_CHART:
                title = "Chart";
                break;
            case ID_TRANSACTIONS:
                title = "Transactions";
                break;
            case ID_CATEGORY:
                title = "Category";
                break;
        }
        getSupportActionBar().setTitle(title);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton setting_fab=findViewById(R.id.settings);
        setting_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(), page_setting.class);
                startActivity(intent);
            }
        });
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomnavigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.outline_home_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CHART, R.drawable.graph_svgrepo_com));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_TRANSACTIONS, R.drawable.transactions_svgrepo_com));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CATEGORY, R.drawable.category_svgrepo_com));

        replaceFragment(new Home_Fragment());


        bottomNavigation.setOnShowListener(item -> {
            int itemId = item.getId();
            updateTitle(itemId);

            switch (itemId) {
                case ID_HOME:
                    replaceFragment(new Home_Fragment());
                    break;
                case ID_CHART:
                    replaceFragment(new Chart_Fragment());
                    break;
                case ID_TRANSACTIONS:
                    replaceFragment(new Transaction_Fragment());
                    break;
                case ID_CATEGORY:
                    replaceFragment(new Category_Fragment());
                    break;
            }
            return null;
        });

        bottomNavigation.show(ID_HOME, true);

    }
    public void updateBottomNavigation(int itemId) {
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomnavigation);
        bottomNavigation.show(itemId, true);
    }
}
