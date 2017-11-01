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
import edu.neu.madcourse.hangxu.database.dao.UserDao;
import edu.neu.madcourse.hangxu.model.Game;

public class ScoreBoardFragment extends Fragment {

    private UserDao userDao;

    public ScoreBoardFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDao = new UserDao(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View vi = inflater.inflate(R.layout.fragment_score_board, container, false);
        final List<Game> topGames = new ArrayList<>();
        userDao.getTopUserGames(new DataChangeListener() {
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
                ListView listView = vi.findViewById(R.id.score_board_listView);
                listView.setAdapter(new ScoreBoardAdapter(getActivity(), topGames));
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
        return vi;
    }

    private class ScoreBoardAdapter extends ArrayAdapter {

        private final Activity context;
        private final List<Game> topGames;

        public ScoreBoardAdapter(Activity context, List<Game> topGames) {
            super(context, R.layout.fragment_score_board_wrap, topGames);
            this.context = context;
            this.topGames = topGames;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vi = context.getLayoutInflater().inflate(R.layout.fragment_score_board_wrap, null, true);
            Game game = topGames.get(position);
            TextView score = vi.findViewById(R.id.score_user);
            score.setText(String.valueOf(game.getScore()));

            return vi;
        }
    }
}
