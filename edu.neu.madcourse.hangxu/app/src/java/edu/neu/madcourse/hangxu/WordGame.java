package edu.neu.madcourse.hangxu;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

/**
 * Class used for the main menu activity of Scroggle game.
 */

public class WordGame extends Activity {

    private MediaPlayer menuMusic;
    private int menuMusicResId = R.raw.menu_background_music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        menuMusic = MediaPlayer.create(this, menuMusicResId);
        menuMusic.setLooping(true);
        menuMusic.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (menuMusic != null) {
            menuMusic.release();
        }
    }
}
