package xdx.fim.uhk.cz.notes_v2;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by xDx on 22.2.2016.
 */
public class Notes_DB {
    protected static final String DATABASE_NAME = "notepad";
    protected static final int DATABASE_VERSION = 3;

    protected static final String TB_NAME = "notesDate";

    // Speciální hodnota "_id", pro jednodušší použití SimpleCursorAdapteru
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_IMPORTANT = "important";
    public static final String COLUMN_DATE = "datum";
    public static final String COLUMN_DATE_START = "startDate";
    public static final String COLUMN_DONE = "done";

    public static final String[] columns = { COLUMN_ID,COLUMN_TITLE, COLUMN_NOTE,
             COLUMN_IMPORTANT, COLUMN_DATE, COLUMN_DATE_START, COLUMN_DONE };

    protected static final String ORDER_BY = COLUMN_ID + " DESC";

    private SQLiteOpenHelper openHelper;

    public Notes_DB(Context ctx) {
        openHelper = new DatabaseHelper(ctx);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TB_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_TITLE + " TEXT NOT NULL,"
                    + COLUMN_NOTE + " TEXT NOT NULL,"
                    + COLUMN_IMPORTANT + " INTEGER NOT NULL,"
                    + COLUMN_DATE + " INTEGER NOT NULL,"
                    + COLUMN_DATE_START + " INTEGER NOT NULL,"
                    + COLUMN_DONE + " INTEGER NOT NULL"
                    + ");");

        }

        /*
         * Ve skutečnosti je potřeba, abychom uživatelům nemazali data, vytvořit
         * pro každou změnu struktury databáze nějaký upgradovací nedestruktivní
         * SQL příkaz.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public Cursor getNotes(int switchNum, int orderWay) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        if(orderWay==1){
            switch(switchNum){
                case 1: // START DATE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_DATE_START +" ASC");
                case 2: // END DATE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_DATE + " ASC");
                case 3: // TITLE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_TITLE + " ASC");
                default:
                    return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
            }
        } else {
            switch(switchNum){
                case 1: // START DATE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_DATE_START +" DESC");
                case 2: // END DATE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_DATE + " DESC");
                case 3: // TITLE
                    return db.query(TB_NAME, columns, null, null, null, null, COLUMN_TITLE + " DESC");
                default:
                    return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
            }
        }


    }

    public Cursor getNote(int id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        return db.query(TB_NAME, columns, COLUMN_ID + "= ?", selectionArgs,
                null, null, ORDER_BY);
    }

    public boolean deleteNote(long id) {
        System.out.println(id);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        String[] selectionArgs = { String.valueOf(id) };

        int deletedCount = db.delete(TB_NAME, COLUMN_ID + "= ?", selectionArgs);
        db.close();
        return deletedCount > 0;
    }

    public long insertNote(String title, String text, boolean important, long date) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_NOTE, text);
        values.put(COLUMN_IMPORTANT, important);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DATE_START, new Date().getTime()/1000L);
        values.put(COLUMN_DONE, false);

        long id = db.insert(TB_NAME, null, values);
        db.close();
        return id;
    }

    public boolean updateNote(int id, String title, String text, boolean important, long date, boolean done) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_NOTE, text);
        values.put(COLUMN_IMPORTANT, important);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DONE, done);

        String[] selectionArgs = { String.valueOf(id) };
        int updateCount = db.update(TB_NAME,values,COLUMN_ID + "= ?", selectionArgs);
        db.close();
        return updateCount>0;
    }

    public boolean updateNoteDone(int id, boolean done) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        System.out.println("-------------------------------------------------");
        System.out.println(done);
        boolean done2;
        if(done){
            done2=false;
        } else {
            done2=true;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_DONE, done2);
        System.out.println("-------------------------------------------------");
        System.out.println(done2);

        String[] selectionArgs = { String.valueOf(id) };
        int updateCount = db.update(TB_NAME,values,COLUMN_ID + "= ?", selectionArgs);
        db.close();
        return updateCount>0;
    }

    public void close() {
        openHelper.close();
    }

}
