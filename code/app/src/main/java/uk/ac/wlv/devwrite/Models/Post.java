package uk.ac.wlv.devwrite.Models;

import android.net.Uri;

import java.util.UUID;

/**
 * Model that represents a post
 */
public class Post {
    private UUID mId;
    private String mTitle;
    private String mContent;
    private Uri mUri = Uri.EMPTY;

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

    public String getPhotoFileName() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

}
