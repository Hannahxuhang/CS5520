package hangxu.finalproject.cs5520.hikerplus.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hangxu.finalproject.cs5520.hikerplus.R;

/**
 * Fragment class used for representing Chats Fragment of Chat Feature.
 */

public class ChatChatFragment extends Fragment {

    public static ChatChatFragment newInstance() {
        ChatChatFragment fragment = new ChatChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_chat, container, false);
    }
}
