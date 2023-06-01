package uk.ac.wlv.devwrite.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.util.UUID;

import uk.ac.wlv.devwrite.Models.Post;

/**
 * Class that queries the database
 */
public class PostCursorWrapper extends CursorWrapper {
    public PostCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Post getPost() {
        String uuidString = getString(getColumnIndex(PostDbSchema.PostTable.Cols.UUID));
        String title = getString(getColumnIndex(PostDbSchema.PostTable.Cols.TITLE));
        String content = getString(getColumnIndex(PostDbSchema.PostTable.Cols.CONTENT));
        Uri uri = Uri.parse(getString(getColumnIndex(PostDbSchema.PostTable.Cols.URI)));
        Post post = new Post(UUID.fromString(uuidString));
        post.setTitle(title);
        post.setContent(content);
        post.setUri(uri);
        return post;
    }
}
