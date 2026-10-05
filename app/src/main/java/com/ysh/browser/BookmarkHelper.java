package com.ysh.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BookmarkHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "bookmark.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "bookmarks";
    private static final String COL_ID = "_id";
    private static final String COL_TITLE = "title";
    private static final String COL_URL = "url";
    private static final String COL_TIME = "time";

    public BookmarkHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_URL + " TEXT, " +
                COL_TIME + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void addBookmark(String title, String url) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COL_URL + "=?", new String[]{url});
        ContentValues v = new ContentValues();
        v.put(COL_TITLE, title);
        v.put(COL_URL, url);
        v.put(COL_TIME, System.currentTimeMillis());
        db.insert(TABLE, null, v);
        db.close();
    }

    public boolean isBookmarked(String url) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{COL_ID}, COL_URL + "=?", new String[]{url}, null, null, null);
        boolean exists = c.moveToFirst();
        c.close();
        db.close();
        return exists;
    }

    public List<Item> getAll() {
        List<Item> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, COL_TIME + " DESC");
        while (c.moveToNext()) {
            list.add(new Item(
                    c.getInt(c.getColumnIndex(COL_ID)),
                    c.getString(c.getColumnIndex(COL_TITLE)),
                    c.getString(c.getColumnIndex(COL_URL)),
                    c.getLong(c.getColumnIndex(COL_TIME))));
        }
        c.close();
        db.close();
        return list;
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

    public static class Item {
        public int id;
        public String title;
        public String url;
        public long time;
        public Item(int id, String title, String url, long time) {
            this.id = id; this.title = title; this.url = url; this.time = time;
        }
    }
}
