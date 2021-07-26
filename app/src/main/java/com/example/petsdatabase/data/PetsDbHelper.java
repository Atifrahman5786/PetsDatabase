package com.example.petsdatabase.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import static com.example.petsdatabase.data.PetsContract.PetsInfo.COLUMN_PET_BREED;
import static com.example.petsdatabase.data.PetsContract.PetsInfo.COLUMN_PET_GENDER;
import static com.example.petsdatabase.data.PetsContract.PetsInfo.COLUMN_PET_NAME;
import static com.example.petsdatabase.data.PetsContract.PetsInfo.COLUMN_PET_WEIGHT;
import static com.example.petsdatabase.data.PetsContract.PetsInfo.TABLE_NAME;

public class PetsDbHelper extends SQLiteOpenHelper {

    public static final String SQL_DELETE_ENTRY = "DROP TABLE IF EXIST " +
            TABLE_NAME;
    public static final String Database_Name = "PetsDatabase.db";
    public static final int Database_Version = 1;
    public PetsDbHelper(@Nullable Context context) {
        super(context, Database_Name, null, Database_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRY = "CREATE TABLE " + TABLE_NAME
                + " (" +   " _id INTEGER PRIMARY KEY , "  +
                COLUMN_PET_NAME + " TEXT NOT NULL" + ", " + COLUMN_PET_BREED +
                " TEXT" + ", " + COLUMN_PET_GENDER + " INTEGER NOT NULL" + ", " +
                COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0" + ");";
        db.execSQL(SQL_CREATE_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(PetsContract.PetsInfo.SQL_DELETE_ENTRY);
        //onCreate(db);
    }
}
