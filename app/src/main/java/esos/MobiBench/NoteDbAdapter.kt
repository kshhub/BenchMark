package esos.MobiBench

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class NotesDbAdapter(private val mCtx: Context) {
    private var mDbHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
            )
            db.execSQL("DROP TABLE IF EXISTS notes")
            onCreate(db)
        }
    }

    @Throws(SQLException::class)
    fun open(): NotesDbAdapter {
        mDbHelper = DatabaseHelper(mCtx)
        mDb = mDbHelper!!.writableDatabase
        return this
    }

    fun insert_DB(
        _id: Int,
        date: String?,
        type: String?,
        has_result: Int,
        act: String?,
        io: String?,
        idl: String?,
        ct_total: String?,
        ct_vol: String?,
        thrp: String?,
        exp_name: String?
    ): Long {
        val initialValues = ContentValues()
        initialValues.put(KEY_ID, _id)
        initialValues.put(KEY_DATE, date)
        initialValues.put(KEY_TYPE, type)
        initialValues.put(KEY_HAS_RESULT, has_result)
        initialValues.put(KEY_ACT, act)
        initialValues.put(KEY_IO, io)
        initialValues.put(KEY_IDL, idl)
        initialValues.put(KEY_CT_TOTAL, ct_total)
        initialValues.put(KEY_CT_VOL, ct_vol)
        initialValues.put(KEY_THRP, thrp)
        initialValues.put(KEY_EXP_NAME, exp_name)
        return mDb!!.insert(DATABASE_TABLE, null, initialValues)
    }

    fun getNum():Int{
        val strsql = "select $KEY_ID from mobidb"
        val db = mDbHelper!!.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToLast()
        val pos:Int = cursor.getInt(0)
        cursor.close()
        db.close()
        return pos
    }

    fun isEmpty():Boolean{
        val strsql = "select * from mobidb"
        val db = mDbHelper!!.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        if(cursor.moveToFirst()){
            return false
        }else{
            return true
        }
    }

    fun findthrp(kid: Int): String {
        val strsql = "select $KEY_THRP from mobidb where $KEY_ID='$kid'"
        val db = mDbHelper!!.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        val cursorlast = db.rawQuery(strsql, null)
        var str: String = ""
        cursor.moveToFirst()
        cursorlast.moveToLast()
        while (cursor.position!=cursorlast.position+1) {
            str = str + cursor.getString(0) + " "
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return str
    }

    @Throws(SQLException::class)
    fun fetchNote(rowId: Long): Cursor? {
        val mCursor = mDb!!.query(
            true,
            DATABASE_TABLE,
            arrayOf(
                KEY_ID,
                KEY_DATE,
                KEY_TYPE,
                KEY_HAS_RESULT,
                KEY_ACT,
                KEY_IO,
                KEY_IDL,
                KEY_CT_TOTAL,
                KEY_CT_VOL,
                KEY_THRP,
                KEY_EXP_NAME
            ),
            KEY_ID + "=" + rowId,
            null,
            null,
            null,
            null,
            null
        )
        mCursor?.moveToFirst()
        return mCursor
    } /*
    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }*/

    companion object {
        const val KEY_ID = "_id"
        const val KEY_DATE = "date"
        const val KEY_TYPE = "type"
        const val KEY_HAS_RESULT = "has_result"
        const val KEY_ACT = "act"
        const val KEY_IO = "io"
        const val KEY_IDL = "idl"
        const val KEY_CT_TOTAL = "ct_total"
        const val KEY_CT_VOL = "ct_vol"
        const val KEY_THRP = "thrp"
        const val KEY_EXP_NAME = "exp_name"
        private const val TAG = "MobiDB"

        /**
         * Database creation sql statement
         */
        private const val DATABASE_CREATE = ("create table mobidb " + "("
                + "_id integer, "
                + "date text, "
                + "type text, "
                + "has_result integer, "
                + "act text, "
                + "io text, "
                + "idl text, "
                + "ct_total text, "
                + "ct_vol text, "
                + "thrp text, "
                + "exp_name text not null);")
        private const val DATABASE_NAME = "MobiDB"
        private const val DATABASE_TABLE = "mobidb"
        private const val DATABASE_VERSION = 1
    }
}