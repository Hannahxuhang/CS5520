package hangxu.finalproject.cs5520.hikerplus.authentication;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hangxu.finalproject.cs5520.hikerplus.PostListActivity;
import hangxu.finalproject.cs5520.hikerplus.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_ACTIVITY";
    private static final String PROGRESS_DIALOG_MESSAGE = "Creating Account...";
    private static final String TOAST_USER_NAME = "Please fill in User Name";
    private static final String TOAST_EMAIL = "Please fill in Email";
    private static final String TOAST_PASSWORD = "Please fill in Password";
    private static final String STATUS_DEFAULT_VALUE = "Say Something...";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;

    private EditText userName;
    private EditText email;
    private EditText password;
    private Button registerButton;
    private FirebaseUser user;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Views
        registerButton = (Button) findViewById(R.id.register_button_Register);
        userName = (EditText) findViewById(R.id.userNameEditText);
        email = (EditText) findViewById(R.id.emailEditText_Register);
        password = (EditText) findViewById(R.id.passwordEt_Register);

        // Initialize FirebaseAuth instance and AuthStateListener method
        mAuth = FirebaseAuth.getInstance();

        // Initialize FirebaseDatabase
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("MUsers");

        // Initialize ProgressDialog
        mProgressDialog = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "create account");
                createAccount();
            }
        });
    }

    private void createAccount() {
        final String userNameText = userName.getText().toString().trim();
        final String emailText = email.getText().toString().trim();
        final String pwdText = password.getText().toString().trim();

        if (!TextUtils.isEmpty(userNameText) && !TextUtils.isEmpty(emailText)
                && !TextUtils.isEmpty(pwdText)) {
            // Show the register progress
            mProgressDialog.setMessage(PROGRESS_DIALOG_MESSAGE);
            mProgressDialog.show();

            // Create a new account with email and password
            mAuth.createUserWithEmailAndPassword(emailText, pwdText)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference curUserDb = mDatabaseReference.child(userId);

                            curUserDb.child("username").setValue(userNameText);
                            curUserDb.child("status").setValue(STATUS_DEFAULT_VALUE);
                            curUserDb.child("profile").setValue("default");
                            curUserDb.child("small_image").setValue("default");
                            curUserDb.child("userId").setValue(userId);

                            mProgressDialog.dismiss();

                            // direct user to PostListActivity
                            Intent intent = new Intent(RegisterActivity.this, PostListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
        } else {
            if (TextUtils.isEmpty(userNameText)) {
                Toast.makeText(RegisterActivity.this, TOAST_USER_NAME, Toast.LENGTH_SHORT)
                        .show();
            }

            if (TextUtils.isEmpty(emailText)) {
                Toast.makeText(RegisterActivity.this, TOAST_EMAIL, Toast.LENGTH_SHORT)
                        .show();
            }

            if (TextUtils.isEmpty(pwdText)) {
                Toast.makeText(RegisterActivity.this, TOAST_PASSWORD, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
