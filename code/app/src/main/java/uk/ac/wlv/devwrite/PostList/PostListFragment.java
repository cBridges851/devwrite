package uk.ac.wlv.devwrite.PostList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.PostEditor.PostActivity;
import uk.ac.wlv.devwrite.R;
import uk.ac.wlv.devwrite.Search.SearchHelper;

public class PostListFragment extends Fragment {
    private RecyclerView mPostRecyclerView;
    private PostAdapter mPostAdapter;
    private List<Post> posts;
    private MaterialButton mCreatePostButton;
    private Menu mMenu;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list, menu);

        mMenu = menu;
        mMenu.findItem(R.id.option_deselect_all).setVisible(false);
        mMenu.findItem(R.id.option_delete).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_search) {
            mPostAdapter.deselectAll();
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) item.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    List<Post> results = new SearchHelper().getResults(query, posts);
                    updateUI(results);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    List<Post> results = new SearchHelper().getResults(newText, posts);
                    updateUI(results);
                    return false;
                }
            });
        }

        if (item.getItemId() == R.id.option_select_all) {
            mPostAdapter.selectAll();
            item.setVisible(false);
            mMenu.findItem(R.id.option_deselect_all).setVisible(true);
            mMenu.findItem(R.id.option_delete).setVisible(true);
        }

        if (item.getItemId() == R.id.option_deselect_all) {
            mPostAdapter.deselectAll();
            MenuItem selectAllMenuItem = mMenu.findItem(R.id.option_select_all);
            selectAllMenuItem.setVisible(true);
            mMenu.findItem(R.id.option_deselect_all).setVisible(false);
            mMenu.findItem(R.id.option_delete).setVisible(false);
        }
        
        if (item.getItemId() == R.id.option_delete) {
            mPostAdapter.deleteSelected();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = DatabaseManager.get(getActivity()).getPosts();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mCreatePostButton = view.findViewById(R.id.create_post_button);
        mCreatePostButton.setOnClickListener(buttonView -> {
            Post post = new Post();
            post.setTitle("");
            post.setContent("");
            DatabaseManager.get(getActivity()).addPost(post);
            Intent intent = PostActivity.newIntent(getActivity(), post.getId());
            startActivity(intent);
        });

        mPostRecyclerView = view.findViewById(R.id.post_recycler_view);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        posts = DatabaseManager.get(getActivity()).getPosts();
        updateUI(posts);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        posts = DatabaseManager.get(getActivity()).getPosts();
        updateUI(posts);
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
        public MaterialCheckBox mCheckBox;
        public Post mPost;

        public PostHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_post_title_text_view);
            mContentTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
            mCheckBox = itemView.findViewById(R.id.list_item_selected_checkbox);
        }

        public void bindPost(Post post) {
            mPost = post;
            String title = mPost.getTitle();

            if (title != null && title.length() > 30) {
                title = title.substring(0, 30);
                title = title + "...";
            }

            mTitleTextView.setText(title);
            String content = mPost.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100);
                content = content + "...";
            }

            mContentTextView.setText(content);
            mCheckBox.setChecked(true);
            mCheckBox.setVisibility(View.GONE);
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<Post> mPosts;
        private List<Post> mSelectedPosts;
        private boolean isMultiSelectEnabled;

        public PostAdapter(List<Post> posts) {
            mPosts = posts;
            isMultiSelectEnabled = false;
            mSelectedPosts = new ArrayList<>();
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
            holder.itemView.setOnLongClickListener(listener -> {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true;
                    mMenu.findItem(R.id.option_delete).setVisible(true);
                    setPostAsSelected(holder);
                }

                return true;
            });

            holder.itemView.setOnClickListener(listener -> {
                if (!isMultiSelectEnabled) {
                    Intent intent = PostActivity.newIntent(getActivity(), post.getId());
                    startActivity(intent);
                }

                if (isMultiSelectEnabled) {
                    if (!mSelectedPosts.contains(post)) {
                        setPostAsSelected(holder);
                    } else {
                        setPostAsDeselected(holder);
                    }
                }

                if (!areAnyPostsSelected()) {
                    isMultiSelectEnabled = false;
                    mMenu.findItem(R.id.option_delete).setVisible(false);
                }

                MenuItem selectAllMenuItem = mMenu.findItem(R.id.option_select_all);
                MenuItem deselectAllMenuItem = mMenu.findItem(R.id.option_deselect_all);

                if (Objects.equals(mPosts.size(), mSelectedPosts.size())) {
                    selectAllMenuItem.setVisible(false);
                    deselectAllMenuItem.setVisible(true);
                } else {
                    selectAllMenuItem.setVisible(true);
                    deselectAllMenuItem.setVisible(false);
                }
            });
        }

        public void selectAll() {
            isMultiSelectEnabled = true;

            for (int i = 0; i < mPosts.size(); i++) {
                PostHolder holder = (PostHolder) mPostRecyclerView.getChildViewHolder(mPostRecyclerView.getChildAt(i));
                setPostAsSelected(holder);
            }
        }

        public void deselectAll() {
            isMultiSelectEnabled = false;

            for (int i = 0; i < mPosts.size(); i++) {
                PostHolder holder = (PostHolder) mPostRecyclerView.getChildViewHolder(mPostRecyclerView.getChildAt(i));
                setPostAsDeselected(holder);
            }
        }

        private boolean areAnyPostsSelected() {
            for (Post post : mPosts) {
                if (mSelectedPosts.contains(post)) {
                    return true;
                }
            }

            return false;
        }

        private void setPostAsSelected(PostHolder holder) {
            int color = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorSurfaceVariant
            );
            holder.itemView.setBackgroundColor(color);
            holder.mCheckBox.setVisibility(View.VISIBLE);

            if (!mSelectedPosts.contains(holder.mPost)) {
                mSelectedPosts.add(holder.mPost);
            }
        }

        private void setPostAsDeselected(PostHolder holder) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.mCheckBox.setVisibility(View.GONE);
            mSelectedPosts.remove(holder.mPost);
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        public void deleteSelected() {
            for (Post post : mSelectedPosts) {
                DatabaseManager.get(getActivity()).deletePost(post);
                mPosts.remove(post);
            }

            mSelectedPosts = new ArrayList<>();
            notifyDataSetChanged();
        }
    }
}
