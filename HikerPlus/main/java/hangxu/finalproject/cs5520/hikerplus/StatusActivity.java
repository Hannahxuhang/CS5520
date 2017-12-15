package hangxu.finalproject.cs5520.hikerplus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "STATUS_ACTIVITY";
    private static final String TOAST_STATUS_UPDATE_SUCCESS = "Status Update Successfully!";
    private static final String TOAST_STATUS_UPDATE_FAIL = "Status Update Failed";

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    EditText statusEt;
    Button updateStatusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statusEt = (EditText) findViewById(R.id.updateStatusEt);
        updateStatusButton = (Button) findViewById(R.id.updateStatusButton);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid);

        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusText = statusEt.getText().toString().trim();

                mDatabase.child("status").setValue(statusText)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(StatusActivity.this, TOAST_STATUS_UPDATE_SUCCESS, Toast.LENGTH_LONG)
                                            .show();
                                    Intent intent = new Intent(StatusActivity.this, PostListActivity.class);
                                    startActivity(intent);
                                } else {
                                    Log.w(TAG, "Status update failed");
                                    Toast.makeText(StatusActivity.this, TOAST_STATUS_UPDATE_FAIL, Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
            }
        });
    }
}
