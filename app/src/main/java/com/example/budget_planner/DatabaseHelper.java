package com.example.budget_planner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "your_database_name";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create tables and initial data
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create your tables here
        // Example: db.execSQL("CREATE TABLE IF NOT EXISTS your_table (id INTEGER PRIMARY KEY, name TEXT);");
    }

    // Upgrade the database if needed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         Handle upgrades here
         Example: db.execSQL("DROP TABLE IF EXISTS your_table;");
         onCreate(db);
        */
    }
}
