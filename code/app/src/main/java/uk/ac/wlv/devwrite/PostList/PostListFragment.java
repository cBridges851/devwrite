package uk.ac.wlv.devwrite.PostList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import uk.ac.wlv.devwrite.database.DatabaseManager;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.PostEditor.PostActivity;
import uk.ac.wlv.devwrite.R;
import uk.ac.wlv.devwrite.Search.SearchHelper;

/**
 * The fragment that is inside the activity that is displayed when the app is opened.
 * It allows the user to see a list of posts in the RecyclerView, press a button to create a post,
 * search posts, and manage multiple posts.
 */
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
        // When no items are selected in the multiselect, only "Select all should be visible"
        mMenu.findItem(R.id.option_select_all).setVisible(true);
        mMenu.findItem(R.id.option_deselect_all).setVisible(false);
        mMenu.findItem(R.id.option_delete).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_search) {
            mPostAdapter.deselectAll();
            // The kebab menu disappears
            mMenu.findItem(R.id.option_select_all).setVisible(false);
            mMenu.findItem(R.id.option_deselect_all).setVisible(false);
            mMenu.findItem(R.id.option_delete).setVisible(false);
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

            searchView.setOnQueryTextFocusChangeListener((listener, hasFocus) -> {
                if (!hasFocus) {
                    mMenu.findItem(R.id.option_select_all).setVisible(true);
                }
            });
        }

        if (item.getItemId() == R.id.option_select_all) {
            mPostAdapter.selectAll();
            // When multiselect is enabled (which is what happens when the user presses select all),
            // only "Select All" shouldn't be visible anymore, and "Deselect All" and "Delete" should
            // be instead
            item.setVisible(false);
            mMenu.findItem(R.id.option_deselect_all).setVisible(true);
            mMenu.findItem(R.id.option_delete).setVisible(true);
        }

        if (item.getItemId() == R.id.option_deselect_all) {
            mPostAdapter.deselectAll();
            MenuItem selectAllMenuItem = mMenu.findItem(R.id.option_select_all);
            // When "Deselect All" is pressed, multiselect should be disabled, meaning "Select All"
            // should become visible, and "Deselect All" and "Delete shouldn't"
            selectAllMenuItem.setVisible(true);
            mMenu.findItem(R.id.option_deselect_all).setVisible(false);
            mMenu.findItem(R.id.option_delete).setVisible(false);
        }
        
        if (item.getItemId() == R.id.option_delete) {
            // Deletes all the items that are selected
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

        // Retrieving all the widgets that are on the fragment and initialising them
        mCreatePostButton = view.findViewById(R.id.create_post_button);
        mCreatePostButton.setOnClickListener(buttonView -> {
            Post post = new Post();
            // Setting default items
            post.setTitle("");
            post.setContent("");
            // Creates a new post so it can be "edited" in the post editor screen
            DatabaseManager.get(getActivity()).addPost(post);
            Intent intent = PostActivity.newIntent(getActivity(), post.getId());
            startActivity(intent);
        });

        // Displays all the posts
        mPostRecyclerView = view.findViewById(R.id.post_recycler_view);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        posts = DatabaseManager.get(getActivity()).getPosts();
        updateUI(posts);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Displays the latest posts when the activity and fragment are resumed, such as after they
        // have rotated the phone or come back to the app after being on a different one
        posts = DatabaseManager.get(getActivity()).getPosts();
        updateUI(posts);
    }

    /**
     * Displays all the latest posts in the recycler view
     * @param posts the posts that should be displayed
     */
    private void updateUI(List<Post> posts) {
        if (mPostAdapter == null) {
            mPostAdapter = new PostAdapter(posts);
            mPostRecyclerView.setAdapter(mPostAdapter);
        } else {
            mPostAdapter.setPosts(posts);
            mPostAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Class that represents a post in the recycler view
     */
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

        /**
         * Attaches a post to a recycler view holder
         * @param post the post to bind
         */
        public void bindPost(Post post) {
            mPost = post;
            String title = mPost.getTitle();

            // Shortens the title and content to ensure that the holders do not become too tall
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
            // Checkbox used to indicate the item has been selected
            mCheckBox.setChecked(true);
            mCheckBox.setVisibility(View.GONE);

            // Checkbox remains checked for aesthetic - it is either there and checked or not there at all
            // Enables the user to deselect a post
            mCheckBox.setOnCheckedChangeListener((listener, value) -> {
                mCheckBox.setChecked(true);
                mPostAdapter.handlePostClick(this);
            });
        }
    }

    /**
     * Manages the holders that display the posts
     */
    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        /**
         * List of all the posts that are displayed
         */
        private List<Post> mPosts;
        /**
         * List of all the posts that have been selected in the multiselect
         */
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
            // Enables a post to be displayed in the recycler view
            View view = layoutInflater.inflate(R.layout.list_item_post, parent, false);
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.bindPost(post);

            // The mSelectedPosts array may not be empty, e.g. after the user has returned to DevWrite
            // after being on another app.
            // This ensures the multiselect persists
            boolean postHasPreviouslyBeenSelected = false;

            for (Post selectedPost: mSelectedPosts) {
                if (Objects.equals(post.getId(), selectedPost.getId())) {
                    postHasPreviouslyBeenSelected = true;
                    setPostAsSelected(holder);
                }
            }

            // Ensures any posts that were not selected before remain unselected
            if (!postHasPreviouslyBeenSelected) {
                setPostAsDeselected(holder);
            }

            // The first item that has been long clicked triggers the multiselect to be enabled
            holder.itemView.setOnLongClickListener(listener -> {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true;
                    // "Select All" and "Delete" should be visible in the menu because not all items have been selected,
                    // but multiselect is enabled, so multiple posts should be able to be selected
                    mMenu.findItem(R.id.option_delete).setVisible(true);
                    setPostAsSelected(holder);
                }

                return true;
            });

            holder.itemView.setOnClickListener(listener -> {
                handlePostClick(holder);
            });
        }

        /**
         * Decides whether the post editor for a post should be entered or if another item should
         * be selected
         * @param holder the holder of the post that has been clicked
         */
        public void handlePostClick(PostHolder holder) {
            Post post = holder.mPost;

            // If multiselect is not enabled, it should go into the post's post editor
            if (!isMultiSelectEnabled) {
                Intent intent = PostActivity.newIntent(getActivity(), post.getId());
                startActivity(intent);
            }

            // If multiselect is enabled, then it should select it or deselect it based on whether
            // it is selected
            if (isMultiSelectEnabled) {
                boolean postIsCurrentlySelected = false;
                for (Post selectedPost: mSelectedPosts) {
                    if (Objects.equals(selectedPost.getId(), post.getId())) {
                        postIsCurrentlySelected = true;
                    }
                }

                if (postIsCurrentlySelected) {
                    setPostAsDeselected(holder);
                } else {
                    setPostAsSelected(holder);
                }
            }

            // Disables multiselect if multiselect is not enabled, so posts can be entered
            // into again on click
            if (!areAnyPostsSelected()) {
                isMultiSelectEnabled = false;
                mMenu.findItem(R.id.option_delete).setVisible(false);
            }

            MenuItem selectAllMenuItem = mMenu.findItem(R.id.option_select_all);
            MenuItem deselectAllMenuItem = mMenu.findItem(R.id.option_deselect_all);

            // If all posts are selected, then "Deselect All" should be visible
            if (Objects.equals(mPosts.size(), mSelectedPosts.size())) {
                selectAllMenuItem.setVisible(false);
                deselectAllMenuItem.setVisible(true);
            } else {
                // Otherwise, "Select All" should be visible so the user can select the remaining
                // posts
                selectAllMenuItem.setVisible(true);
                deselectAllMenuItem.setVisible(false);
            }
        }

        /**
         * Enables all the posts to be selected, enabling multiselect
         */
        public void selectAll() {
            isMultiSelectEnabled = true;

            for (int i = 0; i < mPosts.size(); i++) {
                PostHolder holder = (PostHolder) mPostRecyclerView.getChildViewHolder(mPostRecyclerView.getChildAt(i));
                setPostAsSelected(holder);
            }
        }

        /**
         * Enables all posts to be deselected, disabling multiselect
         */
        public void deselectAll() {
            isMultiSelectEnabled = false;

            LinearLayoutManager layoutManager = (LinearLayoutManager) mPostRecyclerView.getLayoutManager();
            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
            // Ensures that the items that are visible are selected
            int difference = lastVisiblePosition - firstVisiblePosition;
            for (int i = 0; i <= difference; i++) {
                PostHolder holder = (PostHolder) mPostRecyclerView.getChildViewHolder(
                        mPostRecyclerView.getChildAt(i)
                );
                setPostAsDeselected(holder);
            }
        }

        /**
         * Iterates through every post to see if any are selected
         * @return true or false depending on if any posts are selected
         */
        private boolean areAnyPostsSelected() {
            for (Post post : mPosts) {
                for (Post selectedPost: mSelectedPosts) {
                    if (Objects.equals(post.getId(), selectedPost.getId())) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Sets an individual post as selected, so highlights it and adds a checkbox
         * @param holder The holder of the post that should be marked as selected
         */
        private void setPostAsSelected(PostHolder holder) {
            // the highlight colour
            int color = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorSurfaceVariant
            );
            holder.itemView.setBackgroundColor(color);
            holder.mCheckBox.setVisibility(View.VISIBLE);

            boolean alreadySelected = false;

            for (Post selectedPost : mSelectedPosts) {
                if (Objects.equals(selectedPost.getId(), holder.mPost.getId())) {
                    alreadySelected = true;
                    break;
                }
            }

            // Only adds to the list of posts if the post is not already in the list, handles
            // scenario where post may have been selected before the user rotated the phone,
            // for example
            if (!alreadySelected) {
                mSelectedPosts.add(holder.mPost);
            }
        }

        /**
         * Sets an individual post as deselected, so changes the background back to normal
         * and removes the checkbox
         * @param holder The holder of the post that should be marked as deselected
         */
        private void setPostAsDeselected(PostHolder holder) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.mCheckBox.setVisibility(View.GONE);

            Post postToRemove = null;

            for (Post post: mSelectedPosts) {
                if (Objects.equals(post.getId(), holder.mPost.getId())) {
                    postToRemove = post;
                    break;
                }
            }

            if (postToRemove != null) {
                mSelectedPosts.remove(postToRemove);
            }
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        /**
         * Empties the list of selected posts and removes them from the recycler view.
         */
        public void deleteSelected() {
            for (Post selectedPost : mSelectedPosts) {
                DatabaseManager.get(getActivity()).deletePost(selectedPost);

                Post postToDelete = new Post();

                for (Post post : mPosts) {
                    if (Objects.equals(post.getId(), selectedPost.getId())) {
                        postToDelete = post;
                    }
                }

                mPosts.remove(postToDelete);
            }

            mSelectedPosts = new ArrayList<>();
            isMultiSelectEnabled = false;
            mMenu.findItem(R.id.option_select_all).setVisible(true);
            mMenu.findItem(R.id.option_delete).setVisible(false);
            mMenu.findItem(R.id.option_deselect_all).setVisible(false);
            notifyDataSetChanged();
        }
    }
}
