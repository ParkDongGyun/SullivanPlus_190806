package com.example.sullivanplus.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDao {
	@Query("SELECT * FROM Note")
	List<Note> getAll();

	@Query("SELECT * FROM Note")
	LiveData<List<Note>> getDataAll();

	@Query("SELECT * From Note WHERE uid == :id")
	Note getNote(int id);

	@Query("SELECT * From Note order by uid desc")
	Note getNoteLast();

	@Query("SELECT * From Note WHERE uid IN (:NoteId)")
	Note loadAllByIds(int NoteId);

	/*@Query("SELECT * FROM ")
	Note findByNote(String title, String detail, String datetime);*/

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Note note);

	@Delete
	void Delete(Note... note);

	@Query("Delete From Note WHERE uid == :id")
	void Deletes(int... id);
}
