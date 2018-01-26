package hr.math.kolokvij2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by tmavra on 26/01/2018.
 */

public class DBAdapter {

    static final String TAG = "DBAdapter";

    static final String DATABASE_NAME = "MyDB";


    static final String TABLE_SLIKE = "slike";
    static final String TABLE_PERIOD = "period";
    static final int DATABASE_VERSION = 2;

    static final String ID = "id";
    static final String NAZIV = "naziv";
    static final String AUTOR = "autor";

    static final String ID_PERIODA = "id_perioda";
    static final String RAZDOBLJE = "razdoblje";
    static final String GLAVNI_PREDSTAVNIK = "glavni_predstavnik";

    static final String CREATE_SLIKE =
            "create table SLIKE (id integer primary key autoincrement, "
                    + "naziv text not null, autor text not null);";
    static final String CREATE_PERIOD =
            "create table PERIOD (id_perioda integer primary key autoincrement, "
                    + "razdoblje text not null, glavni_predstavnik text not null);";


    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_SLIKE);
                db.execSQL(CREATE_PERIOD);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading db from" + oldVersion + "to"
                    + newVersion);
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    //---opens the database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close() {
        DBHelper.close();
    }


    public long insertSlika(String naziv, String autor) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(NAZIV, naziv);
        initialValues.put(AUTOR, autor);
        return db.insert(TABLE_SLIKE, null, initialValues);

    }

    public long insertPeriod(String razdoblje, String predstavnik) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(RAZDOBLJE, razdoblje);
        initialValues.put(GLAVNI_PREDSTAVNIK, predstavnik);
        return db.insert(TABLE_PERIOD, null, initialValues);

    }


    public Cursor getAllSlike() {
        return db.query(TABLE_SLIKE, new String[]{ID, NAZIV, AUTOR},
                null, null, null, null, null);
    }

    public Cursor getAllRazdoblja(){
        return db.query(TABLE_PERIOD, new String[]{ID_PERIODA, RAZDOBLJE, GLAVNI_PREDSTAVNIK},
                null, null, null, null, null);
    }

}

