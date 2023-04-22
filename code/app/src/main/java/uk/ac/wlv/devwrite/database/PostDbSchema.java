package uk.ac.wlv.devwrite.database;

public class PostDbSchema {
    public static class PostTable {
        public static final String NAME = "posts";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String CONTENT = "content";
        }

    }
}
