package edu.neu.madcourse.hangxu;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.hangxu.database.dao.DataChangeListener;
import edu.neu.madcourse.hangxu.database.dao.GameDao;
import edu.neu.madcourse.hangxu.model.Game;

public class LeaderBoardFragment extends Fragment {

    private GameDao gameDao;

    public LeaderBoardFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameDao = new GameDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_leader_board, container, false);
        final List<Game> topGames = new ArrayList<>();
        gameDao.getTopGames(new DataChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        topGames.add(0, snapshot.getValue(Game.class));
                    }
                }
                ListView listView = view.findViewById(R.id.leader_board_listView);
                listView.setAdapter(new LeaderBoardAdapter(getActivity(), topGames));
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
        return view;
    }

    private class LeaderBoardAdapter extends ArrayAdapter {

        private final Activity context;
        private final List<Game> topGames;

        public LeaderBoardAdapter(Activity context, List<Game> topGames) {
            super(context, R.layout.fragment_leader_board_wrap, topGames);
            this.context = context;
            this.topGames = topGames;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = context.getLayoutInflater().inflate(R.layout.fragment_leader_board_wrap, null, true);
            Game game = topGames.get(position);

            TextView userName = vi.findViewById(R.id.leader_username);
            TextView userScore = vi.findViewById(R.id.leader_score);

            userName.setText(game.getUserName());
            userScore.setText(game.getScore());

            return vi;
        }
    }
}
