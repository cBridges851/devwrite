package uk.ac.wlv.devwrite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.wlv.devwrite.Models.Post;

/**
 * Class that creates, reads, updates and deletes posts in the database.
 */
public class DatabaseManager {
    private static DatabaseManager sDatabaseManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * Returns an instance of this class, there will only be one in the program (singleton pattern)
     * @param context Contains global information about app
     * @return Instance of DatabaseManager
     */
    public static DatabaseManager get(Context context) {
        if (sDatabaseManager == null) {
            sDatabaseManager = new DatabaseManager(context);
        }

        return sDatabaseManager;
    }

    private DatabaseManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new PostBaseHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getContentValues(Post post) {
        ContentValues values = new ContentValues();
        values.put(PostDbSchema.PostTable.Cols.UUID, post.getId().toString());
        values.put(PostDbSchema.PostTable.Cols.TITLE, post.getTitle());
        values.put(PostDbSchema.PostTable.Cols.CONTENT, post.getContent());
        values.put(PostDbSchema.PostTable.Cols.URI, post.getUri().toString());
        return values;
    }

    /**
     * @return posts All the posts that are stored in the database
     */
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        PostCursorWrapper cursor = queryPosts(null, null);

        try {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                posts.add(cursor.getPost());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return posts;
    }

    /**
     * Finds specific posts based on a query
     * @param whereClause the property that should be queried against
     * @param whereArgs the value that should be looked for
     * @return the results of the query
     */
    private PostCursorWrapper queryPosts(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                PostDbSchema.PostTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new PostCursorWrapper(cursor);
    }

    public void addPost(Post post) {
        ContentValues values = getContentValues(post);
        mDatabase.insert(PostDbSchema.PostTable.NAME, null, values);
    }

    public Post getPost(UUID id) {
        PostCursorWrapper cursor = queryPosts(
                PostDbSchema.PostTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try {
            // The post does not exist
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getPost();
        } finally {
            cursor.close();
        }
    }

    public void updatePost(Post post) {
        String uuidString = post.getId().toString();
        ContentValues values = getContentValues(post);
        mDatabase.update(PostDbSchema.PostTable.NAME,
                values,
                PostDbSchema.PostTable.Cols.UUID + " = ?",
                new String[] { uuidString }
        );
    }

    public void deletePost(Post post) {
        String uuidString = post.getId().toString();
        mDatabase.delete(
                PostDbSchema.PostTable.NAME,
                PostDbSchema.PostTable.Cols.UUID + "= ?",
                new String[] { uuidString }
        );
    }

    /**
     * Retrieves a camera photo for the post from the files directory
     * @param post the post the image is for
     * @return the image
     */
    public File getPhotoFile(Post post) {
        File filesDirectory = mContext.getFilesDir();
        return new File(filesDirectory, post.getPhotoFileName());
    }
}
