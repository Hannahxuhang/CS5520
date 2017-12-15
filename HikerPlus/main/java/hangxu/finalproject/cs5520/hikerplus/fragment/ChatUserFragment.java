package hangxu.finalproject.cs5520.hikerplus.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.adapter.ChatUserAdapter;
import hangxu.finalproject.cs5520.hikerplus.model.User;

/**
 * Fragment class used for representing Friends fragment of chat feature.
 */

public class ChatUserFragment extends Fragment {

    private static final String TAG = "CHAT_USER_FRAGMENT";

    private View mView;
    private ViewGroup previousContainer;

    private DatabaseReference mUserDatabase;

    private ChatUserAdapter chatUserAdapter;
    private RecyclerView mRecyclerView;
    private List<User> userList;

    public static ChatUserFragment newInstance() {
        ChatUserFragment fragment = new ChatUserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView != null && this.previousContainer == container) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
        } else {
            try {
                mView = inflater.inflate(R.layout.fragment_chat_user, container, false);
                this.previousContainer = container;
            } catch (InflateException e) {
                Log.w(TAG, "Inflate Exception happend on nous");
                e.printStackTrace();
            }
        }
        //return inflater.inflate(R.layout.fragment_chat_user, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userList = new ArrayList<>();

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers");
        mUserDatabase.keepSynced(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.chat_user_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Add child event listener to mUserDatabase
        mUserDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "Added child to mUserDatabase");

                User user = dataSnapshot.getValue(User.class);

                userList.add(user);

                chatUserAdapter = new ChatUserAdapter(getActivity(), userList);
                mRecyclerView.setAdapter(chatUserAdapter);
                chatUserAdapter.notifyDataSetChanged();

                Log.d(TAG, "userList size: " + userList.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
