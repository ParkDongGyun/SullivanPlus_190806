package com.example.sullivanplus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDBManager extends SQLiteOpenHelper
{
	static final String DB_NOTE = "Note.db";
	static final String TABLE_NOTE = "Note";
	static final int DB_VERSION = 1;
	Context context = null;
	private static NoteDBManager dbManager = null;

	public static NoteDBManager getInstance(Context context){
		if(dbManager == null) {
			dbManager = new NoteDBManager(context,DB_NOTE, null,DB_VERSION);
		}
		return dbManager;
	}

	private NoteDBManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version){
		super(context,dbName,factory,version);
		this.context = context;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTE + "(" + "_id INTEGER PRIMARY KEY," +
				"title TEXT,"+
				"detail TEXT," +
				"datetime TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < newVersion){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);

		}
	}

	public long insert(ContentValues addRowValue){
		return getWritableDatabase().insert(TABLE_NOTE,null,addRowValue);
	}

	public int insertAll(ContentValues[] values){
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		for(ContentValues contentValues : values){
			db.insert(TABLE_NOTE,null,contentValues);
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		return values.length;
	}

	public Cursor query(String [] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
		return getReadableDatabase().query(TABLE_NOTE,columns,selection,selectionArgs,groupBy,having,orderBy);
	}

	public int update(ContentValues updateRowValue,String whereClause, String[] whereArgs){
		return getWritableDatabase().update(TABLE_NOTE,updateRowValue,whereClause,whereArgs);
	}

	public int delete(String whereClause, String[] whereArgs){
		return getWritableDatabase().delete(TABLE_NOTE,whereClause,whereArgs);
	}
}
