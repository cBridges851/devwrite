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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private boolean multiSelectEnabled = false;
    private List<PostHolder> allPostHolders;
    private List<PostHolder> selectedPosts;
    private Menu mMenu;
    private List<MenuItem> multiSelectMenuItems;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list, menu);

        mMenu = menu;
        multiSelectMenuItems.add(mMenu.findItem(R.id.option_delete));

        for (MenuItem menuItem : multiSelectMenuItems) {
            menuItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_search) {
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
            if (selectedPosts.size() != allPostHolders.size()) {
                for (PostHolder postHolder : allPostHolders) {
                    postHolder.isSelected = true;
                    multiSelectEnabled = true;
                    int color = MaterialColors.getColor(
                            requireView(),
                            com.google.android.material.R.attr.colorSurfaceVariant);
                    postHolder.itemView.setBackgroundColor(color);
                    postHolder.mCheckBox.setVisibility(View.VISIBLE);
                    postHolder.mCheckBox.setChecked(true);

                    if (!selectedPosts.contains(postHolder)) {
                        selectedPosts.add(postHolder);
                    }
                }

                item.setTitle(R.string.deselect_all);

                for (MenuItem menuItem : multiSelectMenuItems) {
                    menuItem.setVisible(true);
                }
            } else {
                for (PostHolder postHolder : allPostHolders) {
                    postHolder.isSelected = false;
                    multiSelectEnabled = false;
                    postHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    postHolder.mCheckBox.setVisibility(View.GONE);

                    selectedPosts.remove(postHolder);
                }

                for (MenuItem menuItem : multiSelectMenuItems) {
                    menuItem.setVisible(false);
                }

                item.setTitle(R.string.select_all);

            }
        }

        if (item.getItemId() == R.id.option_delete) {
            for (PostHolder postHolder : selectedPosts) {
                DatabaseManager.get(getActivity()).deletePost(postHolder.mPost);
                posts = DatabaseManager.get(getActivity()).getPosts();
                updateUI(posts);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = DatabaseManager.get(getActivity()).getPosts();
        allPostHolders = new ArrayList<>();
        selectedPosts = new ArrayList<>();
        multiSelectMenuItems = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mCreatePostButton = view.findViewById(R.id.create_post_button);
        mCreatePostButton.setOnClickListener(buttonView -> {
            Post post = new Post();
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
        selectedPosts = new ArrayList<>();
        multiSelectEnabled = false;

        for (MenuItem menuItem : multiSelectMenuItems) {
            menuItem.setVisible(false);
        }

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
        public boolean isSelected = false;

        public PostHolder(View itemView) {
            super(itemView);
            allPostHolders.add(this);
            mTitleTextView = itemView.findViewById(R.id.list_item_post_title_text_view);
            mContentTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
            mCheckBox = itemView.findViewById(R.id.list_item_selected_checkbox);
            mCheckBox.setVisibility(View.GONE);

            itemView.setOnClickListener(event -> {
                if (!multiSelectEnabled) {
                    Intent intent = PostActivity.newIntent(getActivity(), mPost.getId());
                    startActivity(intent);
                    return;
                }

                toggleSelect();
            });

            itemView.setOnLongClickListener(event -> {
                if (!multiSelectEnabled) {
                    multiSelectEnabled = true;
                } else {
                    return false;
                }

                toggleSelect();

                return true;
            });


        }

        private void toggleSelect() {
            if (isSelected) {
                isSelected = false;
                itemView.setBackgroundColor(Color.TRANSPARENT);
                mCheckBox.setVisibility(View.GONE);
                selectedPosts.remove(this);
                MenuItem item = mMenu.findItem(R.id.option_select_all);
                item.setTitle(R.string.select_all);

                if (selectedPosts.isEmpty()) {
                    multiSelectEnabled = false;

                    for (MenuItem menuItem : multiSelectMenuItems) {
                        menuItem.setVisible(false);
                    }
                }

            } else {
                isSelected = true;
                int color = MaterialColors.getColor(
                        requireView(),
                        com.google.android.material.R.attr.colorSurfaceVariant);
                itemView.setBackgroundColor(color);
                mCheckBox.setVisibility(View.VISIBLE);
                mCheckBox.setChecked(true);
                selectedPosts.add(this);

                for (MenuItem menuItem : multiSelectMenuItems) {
                    menuItem.setVisible(true);
                }
            }
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
