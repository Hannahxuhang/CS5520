package edu.neu.madcourse.hangxu;

import android.app.Fragment;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    private static final int COLOR_AVAILABLE = Color.argb(255, 250, 234, 245);
    private static final int COLOR_SELECTED = Color.argb(255, 204, 255, 255);
    private static final int COLOR_LOCKED = Color.argb(255, 149, 149, 183);
    private static final int COLOR_UNAVAILABLE = Color.argb(255, 255, 204, 153);
    private static final int COLOR_HIDE = Color.argb(255, 204, 255, 204);
    private static final int COLOR_PAUSED = Color.argb(255, 204, 204, 0);

    public Integer[][] alphabets;
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
    public ArrayList<Long> wordsDetected = new ArrayList<>();
    public ArrayList<String> words = new ArrayList<>();

    private Tile wholeBoard = new Tile(this);
    private Tile[] largeTiles = new Tile[9];
    private Tile[][] smallTiles = new Tile[9][9];
    private Set<Tile> availableTiles = new HashSet<>();
    private Stack<Tile> currentTiles = new Stack<>();

    private int soundX, soundMiss, soundRewind;
    private SoundPool soundPool;
    private float volume = 1f;
    private int lastLarge;
    private int lastSmall;
    public long[] wordValues = new long[432335];
    public char[] letters = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
            's','t','u','v','w','x','y','z'};

    private int timerValue = 90;
    private int phaseValue = 1;
    private int scoreValue = 0;

    private TextView timer, phase, score;
    private BloomFilter<String> bloomFilter;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes
        setRetainInstance(true);

        loadDictionary();
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
        soundRewind = soundPool.load(getActivity(), R.raw.chimes_glassy, 1);
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

        if (timerValue == 45) {
            timer.setText("Hurry!: " + timerValue);
            timerValue--;
            handler.postDelayed(runnable, 1000);
        } else if (timerValue > 0 && phaseValue == 1) {
            timerValue--;
            handler.postDelayed(runnable, 1000);
        } else if (timerValue == 0 && phaseValue == 1) {
            phaseValue = 2;
            timerValue = 90;
            scoreValue = scoreValue * 2;

            phase.setText("Phase - " + 2);
            hideAllLockedBoard();
            makeAllLockedAbailable();
            updateAllTiles();
            handler.postDelayed(runnable, 1000);
        } else if (timerValue > 0 && phaseValue == 2) {
            timerValue--;
            handler.postDelayed(runnable, 1000);
        } else if (timerValue == 0 && phaseValue == 2) {
            phase.setText("Finished");
            phaseValue = 0;
            handler.removeCallbacks(runnable);
        }
    }

    public void hideAllLockedBoard() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile tile = smallTiles[large][small];
                hideLockedBoard(tile);
            }
        }
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

    public void makeAllLockedAbailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile tile = smallTiles[large][small];
                if (tile.getCharLevel() == COLOR_LOCKED) {
                    tile.setColorMode(COLOR_AVAILABLE);
                    addToAvailable(tile);
                }
            }
        }
    }

    public void loadDictionary() {
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("wordList.txt")));
            char[] letters;
            int counter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                counter++;
                letters = line.toLowerCase().toCharArray();
                wordValues[counter] = encode(letters);
            }
            bufferedReader.close();
            Arrays.sort(wordValues);
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

    public Integer[][] getAlphabets() {
        return alphabets;
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
    private void setAvailableFromLastMove(int large, int small) {
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

    private void setAvailable(int large, int small) {
        Tile tile = smallTiles[large][small];
        if (!(tile.getColorMode() == COLOR_LOCKED || tile.getColorMode() == COLOR_HIDE)) {
            tile.setColorMode(COLOR_AVAILABLE);
            addToAvailable(tile);
        }
    }

    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                setAvailable(large, small);
            }
        }
    }

    private void addToAvailable(Tile tile) {
        tile.animate();
        availableTiles.add(tile);
    }

    private void setSelected(int large, int small) {
        Tile tile = smallTiles[large][small];
        tile.setColorMode(COLOR_SELECTED);
    }

    private void setLocked(Tile tile) {
        tile.setColorMode(COLOR_LOCKED);
        availableTiles.remove(tile);
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
                                if (wordExists()) {
                                    if (phaseValue != 2) {
                                        for (Tile tile : currentTiles) {
                                            setLocked(tile);
                                        }
                                        currentTiles.clear();
                                        hideLockedBoard(smallTile);
                                        setAllAvailable();
                                    } else {
                                        currentTiles.clear();
                                        setAllAvailable();
                                    }
                                    score.setText("Score: " + scoreValue);
                                    updateAllTiles();
                                } else {
                                    scoreValue = scoreValue - (scoreValue / 5);
                                    soundPool.play(soundMiss, volume, volume, 1, 0, 1f);
                                    currentTiles.clear();
                                    setAllAvailable();
                                    updateAllTiles();
                                }
                            } else if (phaseValue == 2) {
                                makeMovePhase2(fLarge, fSmall);
                            } else if (isAdjacent(fLarge, fSmall)) {
                                makeMove(fLarge, fSmall);
                            }
                        } else {
                            soundPool.play(soundMiss, volume, volume, 1, 0, 1f);
                        }
                    }
                });
            }
        }
    }



    public boolean wordExists() {
        long value = 0;
        int wordScore = 0;
        String word = "";

        for (Tile tile : currentTiles) {
            int num = tile.getCharLevel();
            wordScore = wordScore + calScore(num);
            word = word + letters[num - 1];
            if (num < 10) {
                value = value * 10 + num;
            } else {
                value = value * 100 + num;
            }
        }
        if (value == 0) {
            return false;
        } else if (Arrays.binarySearch(wordValues, value) > 0) {
            if (!wordsDetected.contains(value)) {
                if (currentTiles.size() == 9 && phaseValue == 1) {
                    wordScore = wordScore * 2;
                }
                words.add(0, word);
                // refresh score value
                scoreValue = scoreValue + wordScore;
                score.setText("Score: " + scoreValue);
                wordsDetected.add(value);
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

    private void updateAllTiles() {
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

    public long encode(char[] letters) {
        long value = 0;
        if (letters.length == 9) {
            Integer[] nineLetterWord = new Integer[9];
            int i = 0;
            for (char letter : letters) {
                switch (letter) {
                    case 'a':
                        value = value * 10 + 1;
                        nineLetterWord[i] = 1;
                        break;
                    case 'b':
                        value = value * 10 + 2;
                        nineLetterWord[i] = 2;
                        break;
                    case 'c':
                        value = value * 10 + 3;
                        nineLetterWord[i] = 3;
                        break;
                    case 'd':
                        value = value * 10 + 4;
                        nineLetterWord[i] = 4;
                        break;
                    case 'e':
                        value = value * 10 + 5;
                        nineLetterWord[i] = 5;
                        break;
                    case 'f':
                        value = value * 10 + 6;
                        nineLetterWord[i] = 6;
                        break;
                    case 'g':
                        value = value * 10 + 7;
                        nineLetterWord[i] = 7;
                        break;
                    case 'h':
                        value = value * 10 + 8;
                        nineLetterWord[i] = 8;
                        break;
                    case 'i':
                        value = value * 10 + 9;
                        nineLetterWord[i] = 9;
                        break;
                    case 'j':
                        value = value * 100 + 10;
                        nineLetterWord[i] = 10;
                        break;
                    case 'k':
                        value = value * 100 + 11;
                        nineLetterWord[i] = 11;
                        break;
                    case 'l':
                        value = value * 100 + 12;
                        nineLetterWord[i] = 12;
                        break;
                    case 'm':
                        value = value * 100 + 13;
                        nineLetterWord[i] = 13;
                        break;
                    case 'n':
                        value = value * 100 + 14;
                        nineLetterWord[i] = 14;
                        break;
                    case 'o':
                        value = value * 100 + 15;
                        nineLetterWord[i] = 15;
                        break;
                    case 'p':
                        value = value * 100 + 16;
                        nineLetterWord[i] = 16;
                        break;
                    case 'q':
                        value = value * 100 + 17;
                        nineLetterWord[i] = 17;
                        break;
                    case 'r':
                        value = value * 100 + 18;
                        nineLetterWord[i] = 18;
                        break;
                    case 's':
                        value = value * 100 + 19;
                        nineLetterWord[i] = 19;
                        break;
                    case 't':
                        value = value * 100 + 20;
                        nineLetterWord[i] = 20;
                        break;
                    case 'u':
                        value = value * 100 + 21;
                        nineLetterWord[i] = 21;
                        break;
                    case 'v':
                        value = value * 100 + 22;
                        nineLetterWord[i] = 22;
                        break;
                    case 'w':
                        value = value * 100 + 23;
                        nineLetterWord[i] = 23;
                        break;
                    case 'x':
                        value = value * 100 + 24;
                        nineLetterWord[i] = 24;
                        break;
                    case 'y':
                        value = value * 100 + 25;
                        nineLetterWord[i] = 25;
                        break;
                    case 'z':
                        value = value * 100 + 26;
                        nineLetterWord[i] = 26;
                        break;
                }
                i++;
            }
            nineLetterWords.add(nineLetterWord);
        } else {
            for (char letter : letters) {
                switch (letter) {
                    case 'a':
                        value = value * 10 + 1;
                        break;
                    case 'b':
                        value = value * 10 + 2;
                        break;
                    case 'c':
                        value = value * 10 + 3;
                        break;
                    case 'd':
                        value = value * 10 + 4;
                        break;
                    case 'e':
                        value = value * 10 + 5;
                        break;
                    case 'f':
                        value = value * 10 + 6;
                        break;
                    case 'g':
                        value = value * 10 + 7;
                        break;
                    case 'h':
                        value = value * 10 + 8;
                        break;
                    case 'i':
                        value = value * 10 + 9;
                        break;
                    case 'j':
                        value = value * 100 + 10;
                        break;
                    case 'k':
                        value = value * 100 + 11;
                        break;
                    case 'l':
                        value = value * 100 + 12;
                        break;
                    case 'm':
                        value = value * 100 + 13;
                        break;
                    case 'n':
                        value = value * 100 + 14;
                        break;
                    case 'o':
                        value = value * 100 + 15;
                        break;
                    case 'p':
                        value = value * 100 + 16;
                        break;
                    case 'q':
                        value = value * 100 + 17;
                        break;
                    case 'r':
                        value = value * 100 + 18;
                        break;
                    case 's':
                        value = value * 100 + 19;
                        break;
                    case 't':
                        value = value * 100 + 20;
                        break;
                    case 'u':
                        value = value * 100 + 21;
                        break;
                    case 'v':
                        value = value * 100 + 22;
                        break;
                    case 'w':
                        value = value * 100 + 23;
                        break;
                    case 'x':
                        value = value * 100 + 24;
                        break;
                    case 'y':
                        value = value * 100 + 25;
                        break;
                    case 'z':
                        value = value * 100 + 26;
                        break;
                }
            }
        }
        return value;
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
        sb.append(',');
        sb.append(wordsDetected.size());
        for (long value : wordsDetected) {
            sb.append(',');
            sb.append(value);
        }
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
            wordsDetected.clear();
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
                //adapter.notifyDataSetChanged();
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
}
