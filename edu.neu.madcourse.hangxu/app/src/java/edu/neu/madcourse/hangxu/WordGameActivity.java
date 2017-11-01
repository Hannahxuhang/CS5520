package edu.neu.madcourse.hangxu;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import edu.neu.madcourse.hangxu.database.dao.DataChangeListener;
import edu.neu.madcourse.hangxu.database.dao.GameDao;
import edu.neu.madcourse.hangxu.database.dao.UserDao;
import edu.neu.madcourse.hangxu.model.Game;

/**
 * Class used for Scroggle game Activity
 */

public class WordGameActivity extends FragmentActivity {

    public static final String TAG = "Scroggle";
    public static final String KEY_RESTORE = "key_restore";
    public static final String PREF_RESTORE = "pref_restore";

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private GameFragment gameFragment;

    private GameDao gameDao;
    private UserDao userDao;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.fragment_game);

        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        if (restore) {
            String gameData = getPreferences(MODE_PRIVATE).getString(PREF_RESTORE, null);
            if (gameData != null) {
                gameFragment.putState(gameData);
            }
        }
        Log.d(TAG, "restore = " + restore);

        gameDao = new GameDao();
        userDao = new UserDao(getApplicationContext());

        userDao.getLastUserName(new DataChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.getValue().toString();
                game.setUserName(userName);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });

        gameFragment.setGame(game);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer = MediaPlayer.create(this, R.raw.scroggle_background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(null);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        String gameData = gameFragment.getState();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_RESTORE, gameData).commit();
        Log.d(TAG, "state = " + gameData);
    }
}
