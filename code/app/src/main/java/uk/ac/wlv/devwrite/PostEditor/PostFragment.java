package uk.ac.wlv.devwrite.PostEditor;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.UUID;

import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;

public class PostFragment extends Fragment {
    private static final String ARG_POST_ID = "post_id";
    private Post mPost;
    private MaterialToolbar mToolbar;

    public static PostFragment newInstance(UUID postId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST_ID, postId);
        PostFragment fragment = new PostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        UUID postId = (UUID) getArguments().getSerializable(ARG_POST_ID);
//        mPost = DatabaseManager.get(getActivity()).getPost(postId);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        return view;
    }
}
