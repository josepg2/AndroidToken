package com.embeddedproject.projecttoken.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.LoginFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josepg4 on 13/2/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    private static final String DATABASE_NAME = "TokenDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TOKENLIST = "tokenDetails";

    private static final String _ID = "_id";
    private static final String TOKEN = "tokenNumber";
    private static final String STATUS = "tokenStatus";

    private static DbHelper mDbHelper;


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TOKEN_TABLE = "CREATE TABLE " + TABLE_TOKENLIST +
                "(" +
                _ID + " INTEGER PRIMARY KEY ," +
                TOKEN + " INTEGER," +
                STATUS + " INTEGER" +
                ")";
        db.execSQL(CREATE_TOKEN_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOKENLIST);

            onCreate(db);
        }
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    public static synchronized DbHelper getInstance(Context context) {
        if(mDbHelper == null){
            mDbHelper = new DbHelper(context.getApplicationContext());
        }
        return mDbHelper;
    }

    public boolean isTokenPresent(int tokenNumber) {

        boolean tokenDetail = false;

        String USER_DETAIL_SELECT_QUERY = "SELECT * FROM " + TABLE_TOKENLIST + " WHERE " + TOKEN + " = " + Integer.toString(tokenNumber);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(USER_DETAIL_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                tokenDetail = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tokenDetail;

    }

    public void insertTokenDetail(TokenData tokenData) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(TOKEN, tokenData.tokenNumber);
            values.put(STATUS, tokenData.tokenStatus);

            db.insertOrThrow(TABLE_TOKENLIST, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public List<TokenData> getAllTokens() {

        List<TokenData> tokenDetail = new ArrayList<>();

        String USER_DETAIL_SELECT_QUERY = "SELECT * FROM " + TABLE_TOKENLIST;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(USER_DETAIL_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    TokenData tokenData = new TokenData();
                    tokenData.tokenNumber = cursor.getInt(cursor.getColumnIndex(TOKEN));
                    tokenData.tokenStatus = cursor.getInt(cursor.getColumnIndex(STATUS)) == 1 ? true : false;

                    tokenDetail.add(tokenData);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tokenDetail;

    }

    public List<TokenData> getUnAttentedTokens() {

        List<TokenData> tokenDetail = new ArrayList<>();

        String USER_DETAIL_SELECT_QUERY = "SELECT * FROM " + TABLE_TOKENLIST + " WHERE " + STATUS + " = 0";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(USER_DETAIL_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    TokenData tokenData = new TokenData();
                    tokenData.tokenNumber = cursor.getInt(cursor.getColumnIndex(TOKEN));
                    tokenData.tokenStatus = cursor.getInt(cursor.getColumnIndex(STATUS)) == 1 ? true : false;

                    tokenDetail.add(tokenData);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tokenDetail;

    }

    public void updateTokenStatus (int tokenNumber, boolean tokenStatus) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        Log.d("INSIDEDB", Integer.toString(tokenNumber) + " " + tokenStatus);
        try {
            ContentValues values = new ContentValues();
            values.put(STATUS, tokenStatus);

            db.update(TABLE_TOKENLIST, values, TOKEN + "=?", new String[]{Integer.toString(tokenNumber)});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public boolean getTokenStatus (int tokenNumber){

        boolean tokenStatus = Boolean.parseBoolean(null);

        String USER_DETAIL_SELECT_QUERY = "SELECT * FROM " + TABLE_TOKENLIST + " WHERE " + TOKEN + " = " + Integer.toString(tokenNumber);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(USER_DETAIL_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                tokenStatus = cursor.getInt(cursor.getColumnIndex(STATUS)) == 1 ;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tokenStatus;
    }

    void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            db.execSQL("delete from " + TABLE_TOKENLIST);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.d(TAG, "Error while trying to delete  users detail");
        } finally {
            db.endTransaction();
        }
    }

}
