package edu.neu.madcourse.hangxu.database.dao;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.neu.madcourse.hangxu.model.Game;

public class UserDao extends ScroggleDao {

    private final static String CHILD_GAMES = "games";
    private final static String CHILD_SCORE = "score";
    private final static String CHILD_USERS = "users";
    private final static String CHILD_LAST_USER_NAME = "lastUserName";
    private final static int LEADER_NUM = 5;

    private static String clientToken;
    private DatabaseReference databaseReference;

    public UserDao(Context context) {
        super();
        clientToken = FirebaseInstanceId.getInstance().getToken();
        databaseReference = getDatabaseReference().child(CHILD_USERS).child(clientToken);
        databaseReference.keepSynced(true);
    }

    public void setLastUserName(String lastUserName) {
        databaseReference.child(CHILD_LAST_USER_NAME).setValue(lastUserName);
    }

    public void getLastUserName(DataChangeListener listener) {
        readDataFromDatabaseRef(databaseReference.child(CHILD_LAST_USER_NAME), listener);
    }

    public void addUserGame(String gameId, Game game) {
        databaseReference.child(CHILD_GAMES).child(gameId).setValue(game);
    }

    public void getTopUserGames(DataChangeListener listener) {
        Query askTopUserGames = databaseReference.child(CHILD_GAMES).orderByChild(CHILD_SCORE).limitToLast(LEADER_NUM);
        readDataFromQuery(askTopUserGames, listener);
    }
}
