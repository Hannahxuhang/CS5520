package edu.neu.madcourse.hangxu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import edu.neu.madcourse.hangxu.database.dao.DataChangeListener;
import edu.neu.madcourse.hangxu.database.dao.UserDao;

public class RegisterActivity extends AppCompatActivity {

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_database);

        final EditText editText = (EditText) findViewById(R.id.user_name);

        userDao = new UserDao(this);
        userDao.getLastUserName(new DataChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.getValue().toString();
                editText.setText(userName);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }
}
