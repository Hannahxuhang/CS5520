package edu.neu.madcourse.hangxu;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class used for Main Fragment.
 */

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // handle buttons
        View newGameButton = rootView.findViewById(R.id.word_game_new_button);
        View continueButton = rootView.findViewById(R.id.word_game_continue_button);
        View instructionButton = rootView.findViewById(R.id.word_game_instruction_button);
        View acknowledgementsButton = rootView.findViewById(R.id.word_game_acknowledgement_button);

        // set on click event
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WordGameActivity.class);
                startActivity(intent);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WordGameActivity.class);
                intent.putExtra(WordGameActivity.KEY_RESTORE, true);
                startActivity(intent);
            }
        });

        instructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Instructions.class);
                startActivity(intent);
            }
        });

        acknowledgementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Acknowledgements.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
