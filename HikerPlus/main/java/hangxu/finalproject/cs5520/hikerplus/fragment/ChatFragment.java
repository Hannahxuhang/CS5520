package hangxu.finalproject.cs5520.hikerplus.fragment;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.R;

/**
 * Fragment class used for chat feature.
 */

public class ChatFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "CHAT_FRAGMENT";

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    public static final String FRIENDS = "friends";
    public static final String CHATS = "CHATS";

    private boolean isAvatarShown = true;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;

    private CircleImageView mProfile;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private TextView mUserNameTv;
    private TextView mStatusTv;

    private int maxScrollSize;

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();

        Log.d(TAG, "newInstance ChatFragment");

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProfile = (CircleImageView) view.findViewById(R.id.chat_profile_image);
        mTabLayout = (TabLayout) view.findViewById(R.id.chat_tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.chat_user_viewPager);
        mAppBarLayout = (AppBarLayout) view.findViewById(R.id.chat_app_bar);
        mToolbar = (Toolbar) view.findViewById(R.id.chat_toolbar);
        mUserNameTv = (TextView) view.findViewById(R.id.chat_username);
        mStatusTv = (TextView) view.findViewById(R.id.chat_user_status);

        mAppBarLayout.addOnOffsetChangedListener(this);
        maxScrollSize = mAppBarLayout.getTotalScrollRange();

        mViewPager.setAdapter(new TabsAdapter(getActivity().getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        String uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid);

        // Add value event listener to mUserDatabase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String profile = dataSnapshot.child("profile").getValue().toString();

                mUserNameTv.setText(userName);
                mStatusTv.setText(status);

                if (!profile.equals("default")) {
                    Picasso.with(getActivity())
                            .load(profile)
                            .placeholder(R.drawable.profile_img1)
                            .into(mProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (maxScrollSize == 0) {
            maxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(i)) * 100 / maxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && isAvatarShown) {
            isAvatarShown = false;

            mProfile.animate()
                        .scaleY(0)
                        .scaleX(0)
                        .setDuration(200)
                        .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
            isAvatarShown = true;

            mProfile.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .start();
        }
    }

    static class TabsAdapter extends FragmentPagerAdapter {

        private static final int TAB_COUNT = 2;

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return ChatUserFragment.newInstance();
            }
            return ChatChatFragment.newInstance();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return FRIENDS;
            }
            return CHATS;
        }
    }
}
