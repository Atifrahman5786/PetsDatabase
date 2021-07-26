package com.example.petsdatabase;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.petsdatabase.data.PetsContract;
import com.example.petsdatabase.data.PetsDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;
    private static final int EDITOR_LOADER = 1;

    private Uri mCurrentPetUri;

    private boolean mPetHasChanged = false;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesdialog(
            DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(!mPetHasChanged){
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showUnsavedChangesdialog(discardButtonClickListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        Uri currentPetUri = intent.getData();

        if(currentPetUri == null){
            setTitle("Add a Pet");
            invalidateOptionsMenu();
        }
        else{
            setTitle("Edit Pet Info");
            Log.i("Current Id:- " , currentPetUri.toString());
            mCurrentPetUri = currentPetUri;
            //mCurrentPetUri = currentPetUri;
            getLoaderManager().initLoader(EDITOR_LOADER, null, this);
        }

        setupSpinner();


    }
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsContract.PetsInfo.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsContract.PetsInfo.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsContract.PetsInfo.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void savePet(){
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String petWeight = mWeightEditText.getText().toString().trim();

        if(TextUtils.isEmpty(petName)){
            Toast.makeText(this, "Enter a valid pet name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(petWeight)){
            petWeight = "0";
        }

        ContentValues values = new ContentValues();

        values.put(PetsContract.PetsInfo.COLUMN_PET_NAME, petName);
        values.put(PetsContract.PetsInfo.COLUMN_PET_BREED, petBreed);
        values.put(PetsContract.PetsInfo.COLUMN_PET_GENDER, mGender);
        values.put(PetsContract.PetsInfo.COLUMN_PET_WEIGHT, Integer.parseInt(petWeight));

        if(mCurrentPetUri == null){
            Uri newUri = getContentResolver().insert(PetsContract.PetsInfo.CONTENT_URI, values);
            if (newUri == null){
                Toast.makeText(this, "Pet saved failed", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Pet Saved", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            int rowsUpdated;

            rowsUpdated = getContentResolver().update(mCurrentPetUri,
                    values, null, null);
            if(rowsUpdated == 0)
                Toast.makeText(this, "Update Pet Failed", Toast.LENGTH_SHORT).show();
            else{
                Toast.makeText(this, "Pet Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                // After clicking the save we can exit the activity..
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if(!mPetHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesdialog(discardButtonClickListener);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(mCurrentPetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                "rowid _id",
                PetsContract.PetsInfo.COLUMN_PET_NAME,
                PetsContract.PetsInfo.COLUMN_PET_BREED,
                PetsContract.PetsInfo.COLUMN_PET_WEIGHT,
                PetsContract.PetsInfo.COLUMN_PET_GENDER
        };

        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            mNameEditText.setText(data.getString(data.getColumnIndex(PetsContract.PetsInfo.COLUMN_PET_NAME)));
            mBreedEditText.setText(data.getString(data.getColumnIndex(PetsContract.PetsInfo.COLUMN_PET_BREED)));
            mWeightEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(PetsContract.PetsInfo.COLUMN_PET_WEIGHT))));

            int gender = data.getInt(data.getColumnIndex(PetsContract.PetsInfo.COLUMN_PET_GENDER));

            switch (gender){
                case PetsContract.PetsInfo.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetsContract.PetsInfo.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");

        mGenderSpinner.setSelection(0);
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        int rowsDeleted;
        if(mCurrentPetUri != null){
            rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);
            if(rowsDeleted == 0)
                Toast.makeText(this, R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}