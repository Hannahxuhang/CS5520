package edu.neu.madcourse.hangxu;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Class used for Fragment Control.
 */

public class ControlFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_control, container, false);

        // handle buttons
        View ok = rootView.findViewById(R.id.button_ok);
        View quit = rootView.findViewById(R.id.button_quit);
        View pause = rootView.findViewById(R.id.button_pause);

        // set on click event
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getFragmentManager();
                GameFragment gameFragment = (GameFragment) fm.findFragmentById(R.id.fragment_game);
                gameFragment.currentWord = gameFragment.getCurrentWord(gameFragment.currentTiles);
                Log.d("Scroggle", "currentWord is: " + gameFragment.currentWord);
                if (gameFragment.wordExists()) {
                    Log.d("Scroggle", "word " + gameFragment.currentWord + " exists!");
                    gameFragment.setAllAvailable();
                    Log.d("Scroggle", "set all available");
                    Tile currentTile = gameFragment.currentTiles.peek();
                    int currentLarge = currentTile.getLarge();
                    for (int small = 0; small < 9; small++) {
                        Tile tile = gameFragment.smallTiles[currentLarge][small];
                        gameFragment.setLocked(tile);
                        Log.d("Scroggle", "set locked");
                    }
                    gameFragment.updateAllTiles();
                    while (!gameFragment.currentTiles.isEmpty()) {
                        Tile tile = gameFragment.currentTiles.pop();
                        gameFragment.phaseOneTiles.add(tile);
                    }
                    Log.d("Scroggle", "phaseOneTiles size: " + gameFragment.phaseOneTiles.size());
                    gameFragment.currentWord = "";
                }
                gameFragment.currentWord = "";
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return rootView;
    }
}
