package hangxu.finalproject.cs5520.hikerplus;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import hangxu.finalproject.cs5520.hikerplus.fragment.ChatFragment;
import hangxu.finalproject.cs5520.hikerplus.fragment.PostFragment;
import hangxu.finalproject.cs5520.hikerplus.fragment.RecordFragment;
import hangxu.finalproject.cs5520.hikerplus.fragment.SettingFragment;

public class PostListActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_record:
                                selectedFragment = RecordFragment.newInstance();
                                break;
                            case R.id.navigation_post:
                                selectedFragment = PostFragment.newInstance();
                                break;
                            case R.id.navigation_chat:
                                selectedFragment = ChatFragment.newInstance();
                                break;
                            case R.id.navigation_setting:
                                selectedFragment = SettingFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.content, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        // Manually display Record Fragment after logging in
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, RecordFragment.newInstance());
        transaction.commit();
    }
}
