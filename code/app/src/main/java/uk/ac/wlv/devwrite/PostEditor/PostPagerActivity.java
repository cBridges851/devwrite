package uk.ac.wlv.devwrite.PostEditor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;

public class PostPagerActivity extends androidx.fragment.app.FragmentActivity {
    private static final String EXTRA_POST_ID = "uk.ac.wlv.devwrite.post_id";
    private ViewPager mViewPager;
    private List<Post> mPosts;

    public static Intent newIntent(Context packageContext, UUID crimeID) {
        Intent intent = new Intent(packageContext, PostPagerActivity.class);
        intent.putExtra(EXTRA_POST_ID, crimeID);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_pager);
        UUID postId = (UUID) getIntent().getSerializableExtra(EXTRA_POST_ID);
        mViewPager = findViewById(R.id.activity_post_pager_view_pager);
        mPosts = DatabaseManager.get(this).getPosts();
        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Post post = mPosts.get(position);
                return PostFragment.newInstance(post.getId());
            }

            @Override
            public int getCount() {
                return mPosts.size();
            }
        });

        for (int i = 0; i < mPosts.size(); i++) {
            if (mPosts.get(i).getId().equals(postId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        Post currentPost = DatabaseManager.get(this).getPost(postId);

        int index = -1;

        for (int i = 0; i < mPosts.size(); i++) {
            if (mPosts.get(i).getId().toString().equals(currentPost.getId().toString())) {
                index = i;
                break;
            }
        }

        mViewPager.setCurrentItem(index);
    }
}