package edu.neu.madcourse.hangxu;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import edu.neu.madcourse.hangxu.tools.BloomFilter;

/**
 * Class used for Game Fragment.
 */

public class GameFragment extends Fragment {

    private static final String TAG = "Scroggle";

    static private int largeIds[] = {R.id.large1, R.id.large2, R.id.large3, R.id.large4, R.id.large5,
            R.id.large6, R.id.large7, R.id.large8, R.id.large9,};
    static private int smallIds[] = {R.id.small1, R.id.small2, R.id.small3, R.id.small4, R.id.small5,
            R.id.small6, R.id.small7, R.id.small8, R.id.small9,};

    private static final int COLOR_AVAILABLE = Color.argb(0, 0, 0, 0);
    private static final int COLOR_SELECTED = Color.argb(150, 0, 0, 0);
    private static final int COLOR_LOCKED = Color.argb(150, 102, 0, 102);
    private static final int COLOR_UNAVAILABLE = Color.argb(100, 0, 0, 153);
    private static final int COLOR_HIDE = Color.argb(100, 128, 0, 0);

    public Integer[][] alphabets;
    public char[] letters = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
            's','t','u','v','w','x','y','z'};
    public Integer[][] seed = {{0,1,2,5,4,3,6,7,8},
                               {1,2,4,5,8,7,6,3,0},
                               {4,2,5,8,7,6,3,0,1},
                               {4,7,6,3,0,1,2,5,8},
                               {0,4,6,3,1,2,5,7,8},
                               {3,4,0,1,2,5,8,7,6},
                               {8,7,6,3,4,0,1,2,5},
                               {2,4,1,0,3,6,7,5,8},
                               {5,2,1,4,8,7,6,3,0},
                               {7,6,4,8,5,2,1,3,0},
                               {7,4,8,5,2,1,0,3,6}};

    public ArrayList<Integer[]> nineLetterWords = new ArrayList<>();
    //public ArrayList<Long> wordsDetected = new ArrayList<>();
    public HashSet<String> wordSet = new HashSet<>();
    public ArrayList<String> words = new ArrayList<>();
    public ArrayList<Tile> phaseOneTiles = new ArrayList<>();

    private Tile wholeBoard = new Tile(this);
    private Tile[] largeTiles = new Tile[9];
    public Tile[][] smallTiles = new Tile[9][9];
    private Set<Tile> availableTiles = new HashSet<>();
    public Stack<Tile> currentTiles = new Stack<>();

    private int soundX, soundMiss, soundRewind;
    private SoundPool soundPool;
    private float volume = 1f;
    private int lastLarge;
    private int lastSmall;

    public String currentWord = "";

    private int timerValue = 90;
    private int phaseValue = 1;
    private int scoreValue = 0;

    private TextView timer, phase, score;
    private Handler handler = new Handler();
    private Runnable runnable;
    private BloomFilter<String> bloomFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes
        setRetainInstance(true);

        loadBitsetFromFile(R.raw.wordlist);

        setWordBoard();
        // set timer, phase, score's value
        timerValue = 90;
        phaseValue = 1;
        scoreValue = 0;

        initGame();

        runnable = new Runnable() {
            public void run() {
                startTimer();
            }
        };

        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        soundX = soundPool.load(getActivity(), R.raw.dictionary, 1);
        soundMiss = soundPool.load(getActivity(), R.raw.jingle_bell, 1);
        soundRewind = soundPool.load(getActivity(), R.raw.hand_bell, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        initViews(rootView);

        timer = rootView.findViewById(R.id.timer);
        score = rootView.findViewById(R.id.score);
        phase = rootView.findViewById(R.id.phase);
        timer.setText(String.valueOf(timerValue));
        phase.setText("Phase - " + 1);
        score.setText("Score: " + scoreValue);

        updateAllTiles();
        runnable.run();
        return rootView;
    }

    public void startTimer() {
        timer.setText(String.valueOf(timerValue));

        if (timerValue > 0 && phaseValue == 1) {
            timerValue--;
            handler.postDelayed(runnable, 1000);
        } else if (timerValue == 0 && phaseValue == 1) {
            phaseValue = 2;
            timerValue = 90;
            scoreValue = scoreValue * 2;
            phase.setText("Phase - " + 2);

            for (int large = 0; large < 9; large++) {
                for (int small = 0; small < 9; small++) {
                    Tile tile = smallTiles[large][small];
                    tile.setColorMode(COLOR_HIDE);
                }
            }
            availableTiles.clear();
            setAvailableFromPhaseOne();
            updateAllTiles();
            handler.postDelayed(runnable, 1000);
        } else if (timerValue > 0 && phaseValue == 2) {
            timerValue--;
            handler.postDelayed(runnable, 1000);
        } else if (timerValue == 0 && phaseValue == 2) {
            phase.setText("Finished");
            phaseValue = 0;
            handler.removeCallbacks(runnable);

            // display the word list in a Toast
            Context context = getActivity().getApplicationContext();
            CharSequence text = getWordList();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
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

    public void hideLockedBoard(Tile smallTile) {
        for (int i = 0; i < 9; i++) {
            int lockedLarge = smallTile.getLarge();
            Tile tile = smallTiles[lockedLarge][i];
            if (!(tile.getColorMode() == COLOR_LOCKED)) {
                availableTiles.remove(tile);
                tile.setColorMode(COLOR_HIDE);
            }
        }
    }

    public void setWordBoard() {
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("wordList.txt")));
            char[] letters;

            while ((line = bufferedReader.readLine()) != null) {
                letters = line.toLowerCase().toCharArray();
                encode(letters);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Integer[][] alphabets = new Integer[9][9];

        Random random = new Random();
        int range = nineLetterWords.size() - 1;
        for (int i = 0; i < 9; i++) {
            alphabets[i] = nineLetterWords.get(random.nextInt(range));
        }
        setAlphabets(alphabets);
    }

    public void setAlphabets(Integer[][] alphabets) {
        this.alphabets = alphabets;
    }

    private void initGame() {
        Log.d(TAG, "init game");
        wholeBoard = new Tile(this);

        // Create all the tiles
        for (int large = 0; large < 9; large++) {
            largeTiles[large] = new Tile(this);

            for (int small = 0; small < 9; small++) {
                smallTiles[large][small] = new Tile(this);
            }
            largeTiles[large].setSubTiles(smallTiles[large]);
        }
        lastLarge = -1;
        lastSmall = -1;
        wholeBoard.setSubTiles(largeTiles);
        setAllUnavailable();
        setAvailableFromLastMove(lastLarge, lastSmall);
    }

    private void setAllUnavailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile tile = smallTiles[large][small];
                if (!currentTiles.contains(tile)) {
                    setUnavailable(large, small);
                }
            }
        }
    }

    private void setUnavailable(int large, int small) {
        Tile tile = smallTiles[large][small];
        if (!(tile.getColorMode() == COLOR_LOCKED || tile.getColorMode() == COLOR_HIDE)) {
            tile.setColorMode(COLOR_UNAVAILABLE);
            availableTiles.remove(tile);
        }
    }

    // Make only the clicked smallBoard Tile available
    public void setAvailableFromLastMove(int large, int small) {
        if (large != -1) {
            for (int i = 0; i < 9; i++) {
                if (i != small) {
                    Tile tile = smallTiles[large][i];
                    if (!currentTiles.contains(tile)) {
                        setAvailable(large, i);
                    }
                }
            }
        }
        // if there is none available, make all tiles available
        else if (availableTiles.isEmpty()) {
            setAllAvailable();
        }
    }

    public void setAvailableFromPhaseOne() {
        for (Tile tile : phaseOneTiles) {
            tile.setColorMode(COLOR_AVAILABLE);
            addToAvailable(tile);
        }
        Log.d(TAG, "availableTiles size: " + availableTiles.size());
    }

    public void setAvailable(int large, int small) {
        Tile tile = smallTiles[large][small];
        if (!(tile.getColorMode() == COLOR_LOCKED || tile.getColorMode() == COLOR_HIDE)) {
            tile.setColorMode(COLOR_AVAILABLE);
            addToAvailable(tile);
            Log.d(TAG, "color mode: " + tile.getColorMode());
        }
    }

    public void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                setAvailable(large, small);
            }
        }
    }

    public void addToAvailable(Tile tile) {
        tile.animate();
        availableTiles.add(tile);
    }

    public void setSelected(int large, int small) {
        Tile tile = smallTiles[large][small];
        tile.setColorMode(COLOR_SELECTED);
    }

    public void setLocked(Tile tile) {
        tile.setColorMode(COLOR_LOCKED);
        availableTiles.remove(tile);
        Log.d(TAG, "lock color mode: " + tile.getColorMode());
    }

    private void makeMove(int large, int small) {
        lastLarge = large;
        lastSmall = small;
        Tile tile = smallTiles[large][small];
        setSelected(large, small);
        currentTiles.push(tile);
        setAllUnavailable();
        setAvailableFromLastMove(large, small);
        updateAllTiles();
    }

    private void makeMovePhase2(int large, int small) {
        lastLarge = large;
        lastSmall = small;
        Tile tile = smallTiles[large][small];
        setSelected(large, small);
        currentTiles.push(tile);
        setUnavailable(large, small);
        updateAllTiles();
    }

    private void initViews(View rootView) {
        wholeBoard.setView(rootView);
        Random random = new Random();

        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(largeIds[large]);
            largeTiles[large].setView(outer);
            Integer[] pos = seed[random.nextInt(10)];

            for (int small = 0; small < 9; small++) {
                ImageButton inner = outer.findViewById(smallIds[pos[small]]);
                final int fLarge = large;
                final int fSmall = pos[small];
                final Tile smallTile = smallTiles[large][pos[small]];
                smallTile.setLarge(large);
                smallTile.setSmall(pos[small]);
                smallTile.setView(inner);
                smallTile.setCharLevel(alphabets[large][small]);

                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smallTile.animate();

                        if (isAvailable(smallTile)) {
                            soundPool.play(soundX, volume, volume, 1, 0, 1f);
                            if (currentTiles.contains(smallTile)) {
                                if (bloomFilter.contains(currentWord)) {
                                    for (Tile tile : currentTiles) {
                                        setLocked(tile);
                                    }
                                    currentTiles.clear();
                                    currentWord = "";
                                    hideLockedBoard(smallTile);
                                    setAllAvailable();

                                    updateAllTiles();
                                } else {
                                    scoreValue = scoreValue - (scoreValue / 5);
                                    soundPool.play(soundMiss, volume, volume, 1, 0, 1f);
                                    currentTiles.clear();
                                    currentWord = "";
                                    setAllAvailable();
                                    updateAllTiles();
                                }
                            } else if (phaseValue == 2) {
                                makeMovePhase2(fLarge, fSmall);
                            } else {
                                if (isAdjacent(fLarge, fSmall)) {
                                    makeMove(fLarge, fSmall);
                                }
                            }
                        } else {
                            soundPool.play(soundRewind, volume, volume, 1, 0, 1f);
                        }
                    }
                });
            }
        }
    }

    public boolean wordExists() {
        int wordScore = 0;

        for (Tile tile : currentTiles) {
            int num = tile.getCharLevel();
            wordScore = wordScore + calScore(num);
        }
        if (!bloomFilter.contains(currentWord)) {
            return false;
        } else {
            Log.d(TAG, currentWord + "word exists!");
            if (!wordSet.contains(currentWord)) {
                if (currentTiles.size() == 9 && phaseValue == 1) {
                    wordScore = wordScore * 2;
                }
                words.add(0, currentWord);
                // refresh score value
                scoreValue = scoreValue + wordScore;
                score.setText("Score: " + scoreValue);
                Log.d(TAG, "set score text!");
                wordSet.add(currentWord);
                return true;
            }
        }
        return false;
    }

    public int calScore(int num) {
        if (num == 26 || num == 17) {
            return 10;
        } else if (num == 24 || num == 10) {
            return 8;
        } else if (num == 11) {
            return 5;
        } else if (num == 25 || num == 6 || num == 8 || num == 22 || num == 23) {
            return 4;
        } else if (num == 2 || num == 3 || num == 13 || num == 16) {
            return 3;
        } else if (num == 4 || num == 7) {
            return 2;
        } else {
            return 1;
        }
    }

    public boolean isAvailable(Tile tile) {
        return availableTiles.contains(tile);
    }

    public void updateAllTiles() {
        wholeBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            largeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                smallTiles[large][small].updateDrawableState();
            }
        }
    }

    public boolean isAdjacent(int large, int small) {
        if (currentTiles.size() != 0) {
            int i = currentTiles.peek().getLarge();
            int j = currentTiles.peek().getSmall();
            return (large == i && isAdjacentHelper(j, small));
        }
        return true;
    }

    public boolean isAdjacentHelper(int i, int j) {
        boolean flag = false;
        switch (i) {
            case 0: if (j == 1 || j == 3 || j == 4) {
                flag = true;
            }
            break;

            case 1: if (j == 0 || j == 2 || j == 3 || j == 4 || j == 5) {
                flag = true;
            }
            break;

            case 2: if (j == 1 || j == 4 || j==5) {
                flag = true;
            }
            break;

            case 3: if (j == 0 || j == 1 || j == 4 || j == 7 || j == 6) {
                flag = true;
            }
            break;

            case 4: if (j == 0 || j == 1 || j == 3 || j == 7 || j == 6 || j == 2 || j == 5 || j == 8) {
                flag = true;
            }
            break;

            case 5: if (j == 1 || j == 7 || j == 4 || j == 2 || j == 8) {
                flag = true;
            }
            break;

            case 6: if (j == 3 || j == 7 || j == 4) {
                flag = true;
            }
            break;

            case 7: if (j == 6 || j == 3 || j == 4 || j == 5 || j == 8) {
                flag = true;
            }
            break;

            case 8: if (j == 4 || j == 5 || j == 7) {
                flag = true;
            }
            break;
        }
        return flag;
    }

    public void encode(char[] letters) {
        if (letters.length == 9) {
            Integer[] nineLetterWord = new Integer[9];
            int i = 0;
            for (char letter : letters) {
                switch (letter) {
                    case 'a':
                        nineLetterWord[i] = 1;
                        break;
                    case 'b':
                        nineLetterWord[i] = 2;
                        break;
                    case 'c':
                        nineLetterWord[i] = 3;
                        break;
                    case 'd':
                        nineLetterWord[i] = 4;
                        break;
                    case 'e':
                        nineLetterWord[i] = 5;
                        break;
                    case 'f':
                        nineLetterWord[i] = 6;
                        break;
                    case 'g':
                        nineLetterWord[i] = 7;
                        break;
                    case 'h':
                        nineLetterWord[i] = 8;
                        break;
                    case 'i':
                        nineLetterWord[i] = 9;
                        break;
                    case 'j':
                        nineLetterWord[i] = 10;
                        break;
                    case 'k':
                        nineLetterWord[i] = 11;
                        break;
                    case 'l':
                        nineLetterWord[i] = 12;
                        break;
                    case 'm':
                        nineLetterWord[i] = 13;
                        break;
                    case 'n':
                        nineLetterWord[i] = 14;
                        break;
                    case 'o':
                        nineLetterWord[i] = 15;
                        break;
                    case 'p':
                        nineLetterWord[i] = 16;
                        break;
                    case 'q':
                        nineLetterWord[i] = 17;
                        break;
                    case 'r':
                        nineLetterWord[i] = 18;
                        break;
                    case 's':
                        nineLetterWord[i] = 19;
                        break;
                    case 't':
                        nineLetterWord[i] = 20;
                        break;
                    case 'u':
                        nineLetterWord[i] = 21;
                        break;
                    case 'v':
                        nineLetterWord[i] = 22;
                        break;
                    case 'w':
                        nineLetterWord[i] = 23;
                        break;
                    case 'x':
                        nineLetterWord[i] = 24;
                        break;
                    case 'y':
                        nineLetterWord[i] = 25;
                        break;
                    case 'z':
                        nineLetterWord[i] = 26;
                        break;
                }
                i++;
            }
            nineLetterWords.add(nineLetterWord);
        }
    }

    /**
     * Create a string containing the state of the game.
     */
    public String getState() {
        handler.removeCallbacks(runnable);
        StringBuilder sb = new StringBuilder();
        sb.append(lastLarge);
        sb.append(',');
        sb.append(lastSmall);
        sb.append(',');
        sb.append(timerValue);
        sb.append(',');
        sb.append(phaseValue);
        sb.append(',');
        sb.append(scoreValue);

        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                sb.append(',');
                sb.append(smallTiles[large][small].getCharLevel());
                sb.append(',');
                sb.append(smallTiles[large][small].getColorMode());
            }
        }
        //sb.append(',');
        /*
        sb.append(wordsDetected.size());
        for (long value : wordsDetected) {
            sb.append(',');
            sb.append(value);
        }*/
        sb.append(',');
        sb.append(availableTiles.size());
        for (Tile tile : availableTiles) {
            sb.append(',');
            sb.append(tile.getLarge());
            sb.append(',');
            sb.append(tile.getSmall());
        }
        sb.append(',');
        sb.append(words.size());
        for (String word : words) {
            sb.append(',');
            sb.append(word);
        }
        sb.append(',');
        sb.append(currentTiles.size());
        for (Tile tile : currentTiles) {
            sb.append(',');
            sb.append(tile.getLarge());
            sb.append(',');
            sb.append(tile.getSmall());
        }
        return sb.toString();
    }

    /**
     * Restore the state of the game from the given string.
     */
    public void putState(String gameData) {
        String[] fields = gameData.split(",");
        int size;
        int index = -1;
        lastLarge = Integer.parseInt(fields[++index]);
        lastSmall = Integer.parseInt(fields[++index]);
        timerValue = Integer.parseInt(fields[++index]);
        phaseValue = Integer.parseInt(fields[++index]);
        scoreValue = Integer.parseInt(fields[++index]);

        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                smallTiles[large][small].setCharLevel(Integer.parseInt(fields[++index]));
                smallTiles[large][small].setColorMode(Integer.parseInt(fields[++index]));
            }
            size = Integer.parseInt(fields[++index]);
            //wordSet.clear();
            for (int i = 0; i < size; i++) {
                int l = Integer.parseInt(fields[++index]);
                int s = Integer.parseInt(fields[++index]);
                availableTiles.add(smallTiles[l][s]);
            }
            size = Integer.parseInt(fields[++index]);
            words.clear();
            for (int i = 0; i < size; i++) {
                String l = fields[++index];
                words.add(l);
            }
            size = Integer.parseInt(fields[++index]);
            currentTiles.clear();
            for (int i = 0; i < size; i++) {
                int l = Integer.parseInt(fields[++index]);
                int s = Integer.parseInt(fields[++index]);
                currentTiles.add(smallTiles[l][s]);
            }
            setAvailableFromLastMove(lastLarge, lastSmall);
            timer = getActivity().findViewById(R.id.timer);
            phase = getActivity().findViewById(R.id.phase);
            score = getActivity().findViewById(R.id.score);
            timer.setText(String.valueOf(timerValue));
            phase.setText("Phase - " + 1);
            score.setText("Score: " + scoreValue);
            updateAllTiles();
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

    public String getCurrentWord(Stack<Tile> currentTiles) {
        String word = "";

        for (Tile tile : currentTiles) {
            int num = tile.getCharLevel();
            word = word + letters[num - 1];
        }
        return word;
    }
}
