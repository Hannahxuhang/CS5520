package hangxu.finalproject.cs5520.hikerplus.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.ChattingActivity;
import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.model.User;

/**
 * Adapter class for chat user list.
 */

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder> {

    private Context context;
    private List<User> userList;

    public ChatUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public ChatUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_user_row, parent, false);

        return new ChatUserViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(ChatUserViewHolder holder, int position) {

        User user = userList.get(position);

        String profileUrl = user.getProfile();

        if (!profileUrl.equals("default")) {
            Picasso.with(context)
                    .load(profileUrl)
                    .placeholder(R.drawable.profile_img1)
                    .into(holder.profile);
        }

        holder.username.setText(user.getUsername());
        holder.status.setText(user.getStatus());
        holder.userId = user.getUserId();
        holder.userNameText = user.getUsername();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Create subclass of viewholder for ChatUserAdapter
    public class ChatUserViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profile;
        public TextView username;
        public TextView status;
        public String userId;
        public String userNameText;

        public ChatUserViewHolder(View view, Context ctx) {
            super(view);

            context = ctx;

            profile = (CircleImageView) view.findViewById(R.id.chat_user_profile);
            username = (TextView) view.findViewById(R.id.chat_user_username);
            status = (TextView) view.findViewById(R.id.chat_user_status_tv);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to ChattingActivity
                    Intent intent = ChattingActivity.newIntent(context, userId, userNameText);
                    context.startActivity(intent);
                }
            });
        }
    }
}
