package edu.neu.madcourse.hangxu;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class BoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment leaderBoardFragment = new LeaderBoardFragment();
        transaction.add(R.id.board_fragment_container, leaderBoardFragment);
        transaction.commit();

        final Button leaderBoardButton = (Button)findViewById(R.id.leader_board_button);
        final Button scoreBoardButton = (Button)findViewById(R.id.score_board_button);

        leaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                transaction1.replace(R.id.board_fragment_container, new LeaderBoardFragment());
                transaction.commit();
            }
        });

        scoreBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                transaction2.replace(R.id.board_fragment_container, new ScoreBoardFragment());
                transaction2.commit();
            }
        });
    }
}
