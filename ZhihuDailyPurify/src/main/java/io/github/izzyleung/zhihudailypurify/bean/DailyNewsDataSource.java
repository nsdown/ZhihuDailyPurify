package io.github.izzyleung.zhihudailypurify.bean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.izzyleung.zhihudailypurify.db.DBHelper;

import java.lang.reflect.Type;
import java.util.List;

public class DailyNewsDataSource {
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_DATE,
            DBHelper.COLUMN_CONTENT
    };

    public DailyNewsDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open()  throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<DailyNews> createDailyNewsList(String date, String content) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DATE, date);
        values.put(DBHelper.COLUMN_CONTENT, content);

        long insertId = database.insert(DBHelper.TABLE_NAME, null,
                values);
        Cursor cursor = database.query(DBHelper.TABLE_NAME,
                allColumns, DBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        List<DailyNews> newsList = cursorToDailyNews(cursor);
        cursor.close();
        return newsList;
    }

    public void deleteDailyNewsList(String date) {
        database.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_DATE
                + " = " + date, null);
    }

    public List<DailyNews> getDailyNewsList(int id) {
        Cursor cursor = database.query(DBHelper.TABLE_NAME,
                allColumns, DBHelper.COLUMN_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        List<DailyNews> newsList = cursorToDailyNews(cursor);
        cursor.close();
        return newsList;
    }

    public List<DailyNews> getDailyNewsList(String date) {
        Cursor cursor = database.query(DBHelper.TABLE_NAME,
                allColumns, DBHelper.COLUMN_DATE + " = " + date, null,
                null, null, null);

        cursor.moveToFirst();
        List<DailyNews> newsList = cursorToDailyNews(cursor);
        cursor.close();
        return newsList;
    }

    public long count() {
        return DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_NAME);
    }

    private List<DailyNews> cursorToDailyNews(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            String string = cursor.getString(2);
            Type listType = new TypeToken<List<DailyNews>>() {

            }.getType();

            Gson gson = new GsonBuilder().create();

            return gson.fromJson(string, listType);
        } else {
            return null;
        }
    }
}
