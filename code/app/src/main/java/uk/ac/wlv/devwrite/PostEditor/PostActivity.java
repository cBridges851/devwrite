package uk.ac.wlv.devwrite.PostEditor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;

public class PostActivity extends AppCompatActivity {
    private static final String EXTRA_POST_ID = "uk.ac.wlv.devwrite.post_id";
    private ViewPager mViewPager;
    private List<Post> mPosts;

    public static Intent newIntent(Context packageContext, UUID crimeID) {
        Intent intent = new Intent(packageContext, PostActivity.class);
        intent.putExtra(EXTRA_POST_ID, crimeID);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_pager);
        UUID postId = (UUID) getIntent().getSerializableExtra(EXTRA_POST_ID);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = PostFragment.newInstance(postId);
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}