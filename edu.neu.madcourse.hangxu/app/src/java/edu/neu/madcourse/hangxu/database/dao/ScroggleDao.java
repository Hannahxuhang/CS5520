package edu.neu.madcourse.hangxu.database.dao;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ScroggleDao {

    private static FirebaseDatabase myDatabase;
    private DatabaseReference databaseReference;

    public ScroggleDao() {
        if (myDatabase == null) {
            myDatabase = FirebaseDatabase.getInstance();
            myDatabase.setPersistenceEnabled(true);
        }
        databaseReference = myDatabase.getReference();
    }

    public void readDataFromDatabaseRef(DatabaseReference databaseReference, final DataChangeListener listener) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void readDataFromQuery(Query query, final DataChangeListener listener) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
