package uk.ac.wlv.devwrite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        Post shortTitleShortContent = new Post();
        shortTitleShortContent.setTitle("Short Title");
        shortTitleShortContent.setContent("Short Content");

        Post longTitleShortContent = new Post();
        longTitleShortContent.setTitle("This is a very long title which is ironically much longer than the content. It might as well be the content of the post to be honest");
        longTitleShortContent.setContent("Short Content");

        Post shortTitleLongContent = new Post();
        shortTitleLongContent.setTitle("Short Title");
        shortTitleLongContent.setContent("Short Content");

        Post longTitleLongContent = new Post();
        longTitleLongContent.setTitle("This is a very long title which goes into a lot of detail on what the post is about. I better be careful not to make it so long that I completely forget what the post is actually supposed to be about in the first place");
        longTitleLongContent.setContent("I see a lot of posts on here in the software engineering space talking about coding. Coding is awesome, but what I hardly see is people talking about how they're planning to architect their project.\n" +
                "\n" +
                "Although we may feel like we have clear ideas in our head of what we're doing and it can be so TEMPTING to get stuck into the code straight away (believe me, I've been there \uD83D\uDE04), it can help to create what's almost like a \"map\" for ourselves and visualise what our solution could look like, helping to make that implementation easier.\n" +
                "\n" +
                "One way I love to do this is via C4 diagrams, and I've made some as part of my university project.\n" +
                "\n" +
                "C4 diagrams are a bit like Google Maps. You have the highest level, and then you can \"zoom in\" to particular bits you want to see. Here are the four levels in C4 diagrams in a nutshell \uD83D\uDC47\uD83C\uDFFB\n" +
                "\n" +
                "C1 - System Context \n" +
                "\n" +
                "This is the top level where you can see what sort of users and external systems interact with the system.\n" +
                "\n" +
                "C2 - Container (not to be confused with Docker containers \uD83D\uDC33)\n" +
                "\n" +
                "This shows the high level building blocks, like the platforms that are within the system. E.g. websites, mobile apps and APIs\n" +
                "\n" +
                "C3 - Component\n" +
                "\n" +
                "The blocks that make up the container, so effectively groups of functionality. This could be in the form of libraries or microservices in the codebase.\n" +
                "\n" +
                "C4 - Code\n" +
                "\n" +
                "Effectively class diagrams, so consists of the different classes with the methods and properties they have and how they interact with others. This type of diagram isn't always made by hand since IDEs often have tools to generate them.\n" +
                "\n" +
                "The software I love using for this is Structurizr - you can write code that specifies what needs to go in the diagram and it creates the boxes and sorts the layout for you, and it's specifically made for C4 diagrams! Alternatively, you can use software like diagrams.net and Visio.\n" +
                "\n" +
                "So that's what C4 diagrams are all about, and I'm using this to help with development now! I started my first development task for this project this week (so I'll hopefully have something to show next week!)!\n" +
                "\n" +
                "Have you come across C4 diagrams before? How do you approach architechting your projects?\n" +
                "\n" +
                "Are you working on anything currently?\n" +
                "\n" +
                "#SoftwareEngineering #SoftwareArchitecture #diagrams #C4Models");

        Post emojis = new Post();
        emojis.setTitle("Title 😁😘😚🤔🤔🥰😋😛😌");
        emojis.setContent("Content 😚🙄😣🎗🎏🎨🎏🎠🎏🛴💓❣💛");

        Post symbols = new Post();
        symbols.setTitle("!\"$%^&*()-_=+{[}]:;@'~#<,>.?/|\\");
        symbols.setContent("!\"$%^&*()-_=+{[}]:;@'~#<,>.?/|\\");

//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(shortTitleShortContent));
//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(longTitleShortContent));
//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(shortTitleLongContent));
//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(longTitleLongContent));
//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(emojis));
//        mDatabase.insert(PostDbSchema.PostTable.NAME, null, getContentValues(symbols));
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
}
