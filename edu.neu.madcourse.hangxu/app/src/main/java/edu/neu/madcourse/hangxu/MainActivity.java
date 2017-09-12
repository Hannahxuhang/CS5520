package edu.neu.madcourse.hangxu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Hang Xu");
    }

    /**
     * Called when the user taps the About button
     * @param view The view About button
     */
    public void aboutMe(View view) {
        Intent intent = new Intent(this, AboutMeActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Generate Error button
     * @param view The view Generate Error button
     */
    public void generateError(View view) {
        throw new RuntimeException("Unfortunately, this App has Stopped!");
    }
}
