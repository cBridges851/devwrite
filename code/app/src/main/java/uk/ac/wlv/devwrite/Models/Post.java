package uk.ac.wlv.devwrite.Models;

import java.util.UUID;

public class Post {
    private UUID mId;
    private String mTitle;
    private String mContent;

    public Post() {
        this(UUID.randomUUID());
    }

    public Post(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }
}
