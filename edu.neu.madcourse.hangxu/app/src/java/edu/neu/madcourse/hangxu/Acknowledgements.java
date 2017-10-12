package edu.neu.madcourse.hangxu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Class used for Acknowledgements activity.
 */

public class Acknowledgements extends AppCompatActivity {
    TextView ack_image;
    TextView ack_code;
    TextView ack_people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledgements);

        ack_image = (TextView) findViewById(R.id.textView8);
        ack_code = (TextView) findViewById(R.id.textView9);
        ack_people = (TextView) findViewById(R.id.textView10);

        ack_image.setText("Icon Source: SketchPad     https://sketch.io/sketchpad/");
        ack_code.setText("Code Reference: StackOverFlow      https://stackoverflow.com/questions/" +
                "1119332/determine-the-size-of-an-inputstream      https://stackoverflow.com/questions/" +
                "32244851/androidjava-lang-outofmemoryerror-failed-to-allocate-a-23970828-byte-allocatio     " +
                "https://stackoverflow.com/questions/31370940/how-to-read-a-single-word-or-line-from-a-text-file-java" +
                "  BloomFilter     http://blog.locut.us/2008/01/12/a-decent-stand-alone-java" +
                " * -bloom-filter-implementation/");
        ack_people.setText("Helpful People: Zhiyao Jin");
    }
}
