package com.example.petsdatabase;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.petsdatabase.data.PetsContract;

public class PetCursorAdapter extends CursorAdapter {


    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtName = view.findViewById(R.id.name);
        TextView txtSummary = view.findViewById(R.id.summary);

        String strName = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsInfo.COLUMN_PET_NAME));
        String strBreed = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsInfo.COLUMN_PET_BREED));

        if(TextUtils.isEmpty(strBreed)){
            strBreed = "Unknown Breed";
        }

        txtName.setText(strName);
        txtSummary.setText(strBreed);
    }
}
