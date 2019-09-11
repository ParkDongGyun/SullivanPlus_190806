package com.example.sullivanplus.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 2)
public abstract class SullivanDatabase extends RoomDatabase {

	public abstract NoteDao noteDao();

}
