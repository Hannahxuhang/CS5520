package edu.neu.madcourse.hangxu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.madcourse.hangxu.fcm.FCMAsyncTask;

public class CongratsActivity extends AppCompatActivity {

    private final static String NOTIFICATION_TITLE = "Congrats!";
    private final static String NOTIFICATION_BODY = "Congratulations!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_congrats);
        Intent intent = getIntent();

        final String gameId = intent.getExtras().getString("gameId");
        final String body = intent.getExtras().getString("body");

        TextView congratsBody = (TextView)findViewById(R.id.congrats_textView);
        congratsBody.setText(body);

        Button congratsButton = (Button)findViewById(R.id.congrats_button);
        if (gameId == null) {
            congratsButton.setText(R.string.button_ok);
        }
        congratsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameId != null) {
                    new FCMAsyncTask(gameId, null, NOTIFICATION_TITLE, NOTIFICATION_BODY).execute();
                }
                closeActivity();
            }
        });
    }

    private void closeActivity() {
        this.finish();
    }
}
