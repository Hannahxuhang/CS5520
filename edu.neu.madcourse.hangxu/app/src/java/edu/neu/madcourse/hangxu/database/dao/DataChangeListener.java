package edu.neu.madcourse.hangxu.database.dao;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface DataChangeListener {

    public void onStart();
    public void onSuccess(DataSnapshot dataSnapshot);
    public void onFailed(DatabaseError databaseError);
}
