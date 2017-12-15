package hangxu.finalproject.cs5520.hikerplus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.adapter.MessageRecyclerAdapter;
import hangxu.finalproject.cs5520.hikerplus.model.Message;

public class ChattingActivity extends AppCompatActivity {

    private static final String TAG = "CHATTING_ACTIVITY";

    private static final String EXTRA_USER_ID = "hangxu.finalproject.cs5520.hikerplus.user_id";
    private static final String EXTRA_USER_NAME = "hangxu.finalproject.cs5520.hikerplus.user_name";

    private static final String I_WROTE = "I wrote...";
    private static final String WROTE = " wrote...";

    private String senderId;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFirebaseDatabaseRef;
    private DatabaseReference mUserDatabase;
    private DatabaseReference senderDatabase;
    String mCurrentUid;
    String mCurrentUsername;

    private Toolbar mToolbar;
    private Button sendMessageButton;
    private RecyclerView mRecyclerView;
    private EditText mMessageEt;

    private LinearLayoutManager linearLayoutManager;
    //private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private MessageRecyclerAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    public static Intent newIntent(Context packageContext, String userId, String userName) {
        Intent intent = new Intent(packageContext, ChattingActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_USER_NAME, userName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // Get send id from intent extra
        senderId = this.getIntent().getStringExtra(this.EXTRA_USER_ID);
        mCurrentUsername = this.getIntent().getStringExtra(this.EXTRA_USER_NAME);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mCurrentUid = mCurrentUser.getUid();

        mFirebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(mCurrentUid);

        senderDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(senderId);

        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.message_recyclerView);

        sendMessageButton = (Button) findViewById(R.id.sendButton);
        mMessageEt = (EditText) findViewById(R.id.messageEdt);

        // Cast the custom Toolbar
        mToolbar = (Toolbar) findViewById(R.id.message_toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);

        final View actionBarView = inflater.inflate(R.layout.custom_toolbar_item, null);

        // Add value event listener to senderDatabase
        senderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String profileUrl = dataSnapshot.child("profile").getValue().toString();

                TextView barName = (TextView) actionBarView.findViewById(R.id.customBarName);
                CircleImageView barProfile = (CircleImageView) actionBarView.findViewById(R.id.customBarCircleImage);

                barName.setText(username);
                Picasso.with(ChattingActivity.this)
                        .load(profileUrl)
                        .placeholder(R.drawable.profile_img1)
                        .into(barProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mActionBar.setCustomView(actionBarView);

        //setUpFirebaseAdapter();

        // Set the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //mRecyclerView.setAdapter(mFirebaseAdapter);

        // Add child event listener to message database
        mUserDatabase.child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);

                String messageSenderId = message.getUserId();
                String messageReceiverId = message.getReceiverId();

                if ((senderId.equals(messageSenderId) && mCurrentUid.equals(messageReceiverId))
                        || (senderId.equals(messageReceiverId) && mCurrentUid.equals(messageSenderId))) {
                    messageList.add(message);
                }

                messageAdapter = new MessageRecyclerAdapter(ChattingActivity.this, messageList, mCurrentUid);
                mRecyclerView.setAdapter(messageAdapter);
                messageAdapter.notifyDataSetChanged();
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

        // Wire up send message Button
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message mMessage = new Message(mCurrentUid, senderId,
                        mMessageEt.getText().toString().trim(),
                        mCurrentUsername);

                        /*
                        mFirebaseDatabaseRef.child("Messages")
                                .push()
                                .setValue(mMessage);*/

                mUserDatabase.child("Messages")
                        .push()
                        .setValue(mMessage);

                senderDatabase.child("Messages")
                        .push()
                        .setValue(mMessage);

                mMessageEt.setText("");
            }
        });
    }

    /*
    // Helper function for setting up FirebaseRecyclerAdapter
    private void setUpFirebaseAdapter() {
        // TODO:
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class, R.layout.message_row,
                MessageViewHolder.class, mFirebaseDatabaseRef.child("Messages")) {
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder, Message message, int position) {

                if (message.getText() != null &&
                        (message.getUserId().equals(senderId) || message.getReceiverId().equals(senderId))) {
                    viewHolder.bindView(message);

                    boolean isMe = message.getUserId().equals(mCurrentUid);

                    if (isMe) {
                        // Move me to the right side
                        viewHolder.profileRight.setVisibility(View.VISIBLE);
                        viewHolder.profile.setVisibility(View.GONE);
                        //viewHolder.messageTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                        //viewHolder.senderTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

                        // Wire up database
                        mUserDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String profileUrl = dataSnapshot.child("profile").getValue().toString();

                                viewHolder.senderTv.setText(I_WROTE);

                                Picasso.with(viewHolder.profile.getContext())
                                        .load(profileUrl)
                                        .placeholder(R.drawable.profile_img1)
                                        .into(viewHolder.profileRight);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {

                        viewHolder.profileRight.setVisibility(View.GONE);
                        viewHolder.profile.setVisibility(View.VISIBLE);
                       // viewHolder.messageTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                       // viewHolder.senderTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

                        senderDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String profileUrl = dataSnapshot.child("profile").getValue().toString();
                                String senderUsername = dataSnapshot.child("username").getValue().toString();

                                viewHolder.senderTv.setText(senderUsername + WROTE);

                                Picasso.with(viewHolder.profile.getContext())
                                        .load(profileUrl)
                                        .placeholder(R.drawable.profile_img1)
                                        .into(viewHolder.profile);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        };
    }

    // Create a subclass of ViewHolder
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public TextView messageTv;
        public TextView senderTv;
        public CircleImageView profile;
        public CircleImageView profileRight;
        public LinearLayout messageContainer;
        public LinearLayout messageRow;

        public MessageViewHolder(View view) {
            super(view);

            mView = view;
        }

        public void bindView(Message mMessage) {

            messageTv = (TextView) mView.findViewById(R.id.message_text);
            senderTv = (TextView) mView.findViewById(R.id.message_sender);
            profile = (CircleImageView) mView.findViewById(R.id.message_profile);
            profileRight = (CircleImageView) mView.findViewById(R.id.senderImageViewRight);

            messageTv.setText(mMessage.getText());
            senderTv.setText(mMessage.getUserName());
        }
    }*/
}
