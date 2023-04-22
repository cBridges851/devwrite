package uk.ac.wlv.devwrite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.database.PostBaseHelper;
import uk.ac.wlv.devwrite.database.PostCursorWrapper;
import uk.ac.wlv.devwrite.database.PostDbSchema;

public class DatabaseManager {
    private static DatabaseManager sDatabaseManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

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
        return values;
    }

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
}
