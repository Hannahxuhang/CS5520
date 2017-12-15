package hangxu.finalproject.cs5520.hikerplus.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hangxu.finalproject.cs5520.hikerplus.PostListActivity;
import hangxu.finalproject.cs5520.hikerplus.R;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LOG_IN_ACTIVITY";
    private static final String TOAST_SIGNED_IN = "Signed In";
    private static final String TOAST_SIGNED_OUT = "Not Signed In";
    private static final String TOAST_SIGNED_IN_SUCCEED = "Signed In Succeed";
    private static final String TOAST_SIGNED_IN_FAILED = "Signed In Failed";
    private static final String TOAST_EMAIL = "Please fill in Email";
    private static final String TOAST_PASSWORD = "Please fill in Password";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText email;
    private EditText password;
    private Button logInButton;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logInButton = (Button) findViewById(R.id.button_logIn_LogIn);
        email = (EditText) findViewById(R.id.emailEditText);
        password = (EditText) findViewById(R.id.passwordEditText);

        // Initialize FirebaseAuth instance and AuthStateListener method
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(LogInActivity.this, TOAST_SIGNED_IN, Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                } else {
                    // User is signed out
                    Toast.makeText(LogInActivity.this, TOAST_SIGNED_OUT, Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Wire up Log In Button
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(email.getText().toString()) &&
                        !TextUtils.isEmpty(password.getText().toString())) {

                    String emailText = email.getText().toString();
                    String pwdText = password.getText().toString();

                    logIn(emailText, pwdText);
                } else {
                    if (TextUtils.isEmpty(email.getText().toString())) {
                        Toast.makeText(LogInActivity.this, TOAST_EMAIL, Toast.LENGTH_SHORT)
                                .show();
                    }

                    if (TextUtils.isEmpty(password.getText().toString())) {
                        Toast.makeText(LogInActivity.this, TOAST_PASSWORD, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void logIn(String emailText, String pwdText) {
        mAuth.signInWithEmailAndPassword(emailText, pwdText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // if sign in successes
                        if (task.isSuccessful()) {
                            Toast.makeText(LogInActivity.this, TOAST_SIGNED_IN_SUCCEED, Toast.LENGTH_LONG)
                                    .show();

                            // direct user to PostListActivity
                            Intent intent = new Intent(LogInActivity.this, PostListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());

                            // if sign in fails
                            Toast.makeText(LogInActivity.this, TOAST_SIGNED_IN_FAILED, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }
}
