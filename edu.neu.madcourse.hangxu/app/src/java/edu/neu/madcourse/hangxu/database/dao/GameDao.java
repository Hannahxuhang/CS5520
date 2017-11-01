package edu.neu.madcourse.hangxu.database.dao;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import edu.neu.madcourse.hangxu.model.Game;

public class GameDao extends ScroggleDao {

    private final static int LEADER_NUM = 5;
    private final static int SINGLE_NUM = 1;
    private final static String CHILD_GAMES = "games";
    private final static String CHILD_SCORE = "score";

    private DatabaseReference databaseReference;

    public GameDao() {
        super();
        databaseReference = getDatabaseReference().child(CHILD_GAMES);
    }

    public String onAddGame(Game game) {
        String gameId = databaseReference.push().getKey();
        databaseReference.child(gameId).setValue(game);
        return gameId;
    }

    public void getTopGames(DataChangeListener listener) {
        Query askTopGames = databaseReference.orderByChild(CHILD_SCORE).limitToLast(LEADER_NUM);
        askTopGames.keepSynced(true);
        readDataFromQuery(askTopGames, listener);
    }

    public void getTopGame(DataChangeListener listener) {
        Query askTopGame = databaseReference.orderByChild(CHILD_SCORE).limitToLast(SINGLE_NUM);
        askTopGame.keepSynced(true);
        readDataFromQuery(askTopGame, listener);
    }
}
