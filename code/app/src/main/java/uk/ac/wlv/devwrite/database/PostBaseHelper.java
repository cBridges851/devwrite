package uk.ac.wlv.devwrite.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class that creates the database
 */
public class PostBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "postBase.db";

    public PostBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Creating a new table for the posts
        sqLiteDatabase.execSQL("CREATE TABLE " + PostDbSchema.PostTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                PostDbSchema.PostTable.Cols.UUID + ", " +
                PostDbSchema.PostTable.Cols.TITLE + ", " +
                PostDbSchema.PostTable.Cols.CONTENT + ", " +
                PostDbSchema.PostTable.Cols.URI +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
