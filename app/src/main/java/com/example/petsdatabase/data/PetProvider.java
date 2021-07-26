package com.example.petsdatabase.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetProvider extends ContentProvider {
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = "hi";

    // Static initializer. This is run the first time anything is called from this class.

    static {
        uriMatcher.addURI(PetsContract.PetsInfo.CONTENT_AUTHORITY, PetsContract.PetsInfo.PATH_PETS, PETS);
        uriMatcher.addURI(PetsContract.PetsInfo.CONTENT_AUTHORITY, PetsContract.PetsInfo.PATH_PETS+ "/#", PET_ID);
    }

    private PetsDbHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new PetsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);
        switch (match){
            case PETS :
                cursor = database.query(PetsContract.PetsInfo.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID :
                selection = "rowid" + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetsContract.PetsInfo.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }


        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsContract.PetsInfo.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsContract.PetsInfo.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPets(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not suported for " + uri);
        }

    }

    private Uri insertPets(Uri uri, ContentValues values) {
        String name = values.getAsString(PetsContract.PetsInfo.COLUMN_PET_NAME);
        if(name == null){
            throw new IllegalArgumentException("Pet name required");
        }
        Integer gender = values.getAsInteger(PetsContract.PetsInfo.COLUMN_PET_GENDER);
        if(gender == null){
            throw new IllegalArgumentException("enter a valid gender");
        }
        Integer weight = values.getAsInteger(PetsContract.PetsInfo.COLUMN_PET_WEIGHT);
        if(weight == null || weight < 0){
            throw new IllegalArgumentException("weight cannotbe negative");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newId = db.insert(PetsContract.PetsInfo.TABLE_NAME, null, values);
        if (newId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowDeleted;
        switch (match){
            case PETS:
                rowDeleted =  db.delete(PetsContract.PetsInfo.TABLE_NAME, selection, selectionArgs);
                if(rowDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowDeleted;
            case PET_ID:
                selection = "rowid" + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowDeleted =  db.delete(PetsContract.PetsInfo.TABLE_NAME, selection, selectionArgs);
                if(rowDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowDeleted;
            default:
                throw new IllegalArgumentException("Error with deleting data");
        }




    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePets(uri, values, selection, selectionArgs);
            case PET_ID:
                selection = "rowid" + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePets(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Error with updating pets with " + uri);
        }

    }

    private int updatePets(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(PetsContract.PetsInfo.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsContract.PetsInfo.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetsContract.PetsInfo.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsContract.PetsInfo.COLUMN_PET_GENDER);
            if (gender == null ) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetsContract.PetsInfo.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetsContract.PetsInfo.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated =  db.update(PetsContract.PetsInfo.TABLE_NAME,
                values, selection, selectionArgs);
        if(rowUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }
}
