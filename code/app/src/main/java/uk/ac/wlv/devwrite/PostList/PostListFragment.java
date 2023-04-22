package uk.ac.wlv.devwrite.PostList;

import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;

public class PostListFragment extends Fragment {
    private RecyclerView mPostRecyclerView;
    private PostAdapter mPostAdapter;
    private List<Post> posts;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = DatabaseManager.get(getActivity()).getPosts();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mPostRecyclerView = view.findViewById(R.id.post_recycler_view);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        posts = DatabaseManager.get(getActivity()).getPosts();
        updateUI(posts);
        return view;
    }

    private void updateUI(List<Post> posts) {
        if (mPostAdapter == null) {
            mPostAdapter = new PostAdapter(posts);
            mPostRecyclerView.setAdapter(mPostAdapter);
        } else {
            mPostAdapter.setPosts(posts);
            mPostAdapter.notifyDataSetChanged();
        }
    }

    private class PostHolder extends RecyclerView.ViewHolder {
        public MaterialTextView mTitleTextView;
        public MaterialTextView mContentTextView;
        public Post mPost;

        public PostHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_post_title_text_view);
            mContentTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
        }

        public void bindPost(Post post) {
            mPost = post;
            mTitleTextView.setText(mPost.getTitle());
            String content = mPost.getContent();
            if (content.length() > 100) {
                content = content.substring(0, 100);
                content = content + "...";
            }

            mContentTextView.setText(content);
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<Post> mPosts;

        public PostAdapter(List<Post> posts) {
            mPosts = posts;
        }

        public void setPosts(List<Post> posts) {
            mPosts = posts;
        }

        @NonNull
        @Override
        public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_post, parent, false);
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.bindPost(post);
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }
    }
}
