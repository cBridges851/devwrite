package uk.ac.wlv.devwrite.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

import uk.ac.wlv.devwrite.Models.Post;

public class PostCursorWrapper extends CursorWrapper {
    public PostCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Post getPost() {
        String uuidString = getString(getColumnIndex(PostDbSchema.PostTable.Cols.UUID));
        String title = getString(getColumnIndex(PostDbSchema.PostTable.Cols.TITLE));
        String content = getString(getColumnIndex(PostDbSchema.PostTable.Cols.CONTENT));
        Post post = new Post(UUID.fromString(uuidString));
        post.setTitle(title);
        post.setContent(content);
        return post;
    }
}
