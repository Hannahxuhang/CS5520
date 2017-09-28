package edu.neu.madcourse.hangxu;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

import edu.neu.madcourse.hangxu.tools.BloomFilter;

/**
 * Class used for Dictionary Activity.
 */

public class MyDictionary extends AppCompatActivity implements OnClickListener {
    final static String TAG = "\n#DEBUG";

    private EditText userInput;
    private TextView wordList;
    private MediaPlayer mediaPlayer;

    private BloomFilter<String> bloomFilter;
    HashSet<String> wordSet = new HashSet<>();

    @Override
    protected void onResume() {
        super.onResume();
        wordList.setText(getWordList());
    }

    /**
     * Helper method used for getting word list.
     * @return the word list which has been detected
     */
    private CharSequence getWordList() {
        String wordListCharSeq = "Word List is: ";
        Iterator<String> iterator = wordSet.iterator();
        while (iterator.hasNext()) {
            wordListCharSeq = wordListCharSeq + iterator.next() + ", ";
        }
        wordListCharSeq = wordListCharSeq.substring(0, wordListCharSeq.length() - 2);
        return wordListCharSeq;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        // set title text of the actionBar
        getSupportActionBar().setTitle("Test Dictionary");

        //load bit set from file "wordlist.txt"
        loadBitsetFromFile(R.raw.wordlist);

        setVolumeControlStream(AudioManager.STREAM_RING);

        wordList = (TextView) findViewById(R.id.textView7);
        userInput = (EditText) findViewById(R.id.editText);

        // use text change listener to keep track of user's input
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence wordSeq, int a1, int a2, int a3) {

            }

            @Override
            public void onTextChanged(CharSequence wordSeq, int a1, int a2, int a3) {
                if (wordSeq.length() > 2) {
                    checkWord(wordSeq.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable wordSeq) {

            }
        });

        View clear = findViewById(R.id.button);
        clear.setOnClickListener(this);

        View acknowledge = findViewById(R.id.button2);
        acknowledge.setOnClickListener(this);
    }

    private void checkWord(String inputWord) {
        inputWord = inputWord.toLowerCase();
        if (bloomFilter.contains(inputWord) && !wordSet.contains(inputWord)) {
            playWordBeep();

            wordSet.add(inputWord);
            wordList.setText(getWordList());
        }
    }

    private void playWordBeep() {
        // release any resources from previous MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.dictionary);
        mediaPlayer.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button:
                userInput = (EditText) findViewById(R.id.editText);
                userInput.setText("");
                wordSet.clear();
                wordList.setText(getWordList());
                break;
            case R.id.button2:
                Intent intent = new Intent(this, Acknowledgements.class);
                startActivity(intent);
                break;
        }
    }

    private BloomFilter<String> loadBitsetFromFile(int fileId) {
        try {
            InputStream inputStream = getResources().openRawResource(fileId);
            int fileLength = inputStream.available();
            Log.d(TAG, "file length = " + fileLength);

            byte[] fileData = new byte[fileLength];
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            dataInputStream.readFully(fileData);
            dataInputStream.close();

            bloomFilter = new BloomFilter<>(0.0001, 450000);
            bloomFilter = BloomFilter.loadBitsetWithByteArray(fileData, bloomFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bloomFilter;
    }
}
