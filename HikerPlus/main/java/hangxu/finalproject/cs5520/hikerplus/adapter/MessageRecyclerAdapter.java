package hangxu.finalproject.cs5520.hikerplus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.model.Message;

/**
 * Adapter class for message RecyclerView.
 */

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder> {

    private static final String I_WROTE = "I wrote...";
    private static final String WROTE = " wrote...";

    private Context context;
    private List<Message> messageList;
    private String mCurrentUserId;

    private DatabaseReference mDatabaseRef;

    public MessageRecyclerAdapter(Context context, List<Message> messageList, String mCurrentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.mCurrentUserId = mCurrentUserId;
        this.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_row, parent, false);

        return new MessageViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int position) {

        Message message = messageList.get(position);

        String senderId = message.getUserId();
        boolean isMe = senderId.equals(mCurrentUserId);

        viewHolder.messageTv.setText(message.getText());

        if (isMe) {
            // Move profile to the right side
            viewHolder.profileRight.setVisibility(View.VISIBLE);
            viewHolder.profile.setVisibility(View.GONE);

            viewHolder.senderTv.setText(I_WROTE);
            viewHolder.messageTv.setTextColor(Color.rgb(84, 100, 71));

            mDatabaseRef.child("MUsers").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String profileUrl = dataSnapshot.child("profile").getValue().toString();

                    if (!profileUrl.equals("default")) {
                        Picasso.with(context)
                                .load(profileUrl)
                                .placeholder(R.drawable.profile_img1)
                                .into(viewHolder.profileRight);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // Remain profile on the left side
            viewHolder.profileRight.setVisibility(View.GONE);
            viewHolder.profile.setVisibility(View.VISIBLE);

            viewHolder.senderTv.setText(message.getUserName() + WROTE);

            mDatabaseRef.child("MUsers").child(senderId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String profileUrl = dataSnapshot.child("profile").getValue().toString();

                    if (!profileUrl.equals("default")) {
                        Picasso.with(context)
                                .load(profileUrl)
                                .placeholder(R.drawable.profile_img1)
                                .into(viewHolder.profile);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Create a subclass of ViewHolder
    class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTv;
        public TextView senderTv;
        public CircleImageView profile;
        public CircleImageView profileRight;

        public MessageViewHolder(View view, Context ctx) {
            super(view);

            context = ctx;

            messageTv = (TextView) view.findViewById(R.id.message_text);
            senderTv = (TextView) view.findViewById(R.id.message_sender);
            profile = (CircleImageView) view.findViewById(R.id.message_profile);
            profileRight = (CircleImageView) view.findViewById(R.id.senderImageViewRight);
        }
    }
}
