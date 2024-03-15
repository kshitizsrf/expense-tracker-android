package com.example.budget_planner;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Detail_Transaction extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_transaction);
        TextView category_name = findViewById(R.id.cat_name);
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        TextView note = findViewById(R.id.note);
        TextView amount = findViewById(R.id.amount);
        FloatingActionButton delete_data = findViewById(R.id.delete);
        FloatingActionButton back=findViewById(R.id.Back);
        ImageView icon=findViewById(R.id.icon);
        int transaction_id=getIntent().getIntExtra("id",0);
        delete_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Detail_Transaction.this);
                builder.setTitle("Delete Transaction!")
                        .setMessage("Are you sure you want to delete this transaction? This action cannot be undone.")
                        .setCancelable(true)
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBHelper mydb = new DBHelper(Detail_Transaction.this);
                                int rowsAffected = mydb.deleteTransaction(transaction_id);
                                if (rowsAffected > 0) {
                                    // Deletion successful
                                    Toast.makeText(Detail_Transaction.this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity
                                } else {
                                    // Deletion failed or no rows affected
                                    Toast.makeText(Detail_Transaction.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        double amountValue = getIntent().getDoubleExtra("amount", 0.0);
        String cat_icon = getIntent().getStringExtra("category_icon");
        String categoryName = getIntent().getStringExtra("category_name");
        String type = getIntent().getStringExtra("type");
        if(type.equals("Expense")){
            amount.setText("-"+String.valueOf(amountValue));
            amount.setTextColor(Color.RED);
        }else{
            amount.setText("+"+String.valueOf(amountValue));
            amount.setTextColor(Color.GREEN);
        }
        // Set compound drawable start
        int drawableResourceId = getResources().getIdentifier(cat_icon, "drawable", getPackageName());
        icon.setImageResource(drawableResourceId);
        category_name.setText(categoryName);

        date.setText(getIntent().getStringExtra("date"));
        time.setText(getIntent().getStringExtra("time"));
        note.setText(getIntent().getStringExtra("note"));
    }
}
