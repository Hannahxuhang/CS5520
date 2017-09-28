package edu.neu.madcourse.hangxu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Hang Xu");

        // get version code and version name of this app
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(String.valueOf(versionCode));
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setText(versionName);
    }

    /**
     * Called when the user clicks the About button
     * @param view The view About button
     */
    public void aboutMe(View view) {
        Intent intent = new Intent(this, AboutMeActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Generate Error button
     * @param view The view Generate Error button
     */
    public void generateError(View view) {
        throw new RuntimeException("Unfortunately, this App has Stopped!");
    }

    /**
     * Called when the user clicks the Dictionary button
     * @param view The view Dictionary button
     */
    public void dictionary(View view) {
        Intent intent = new Intent(this, MyDictionary.class);
        startActivity(intent);
    }
}
