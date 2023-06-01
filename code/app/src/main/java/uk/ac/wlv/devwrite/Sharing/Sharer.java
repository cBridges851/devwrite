package uk.ac.wlv.devwrite.Sharing;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Objects;

/**
 * Handles the sharing of posts to external apps
 */
public class Sharer {
    /**
     * Shares a post to LinkedIn
     * @param source The fragment that triggered this method and requires the share
     * @param content The content that should be in the post or message
     * @param fileUri The URI to the post's image
     */
    public void sharePostToLinkedIn(Fragment source, String content, Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setPackage("com.linkedin.android");
        // Allows text and/or image to be added to the post
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        // Attaches the image if there is one
        if (!Objects.equals(fileUri.toString(), "")) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        }

        try {
            source.startActivity(shareIntent);
        } catch (Exception exception) {
            // The activity would not be able to start if LinkedIn is not installed, hence why it would hit this catch
            Toast.makeText(source.getActivity(), "LinkedIn is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shares a post to email
     * @param source The fragment that triggered this method and requires the share
     * @param title The title of the post that will be put in the subject of the email
     * @param content The content of the post that will be put in the body of the email
     * @param fileUri The URI to the post's image
     */
    public void sharePostToEmail(Fragment source, String title, String content, Uri fileUri) {
        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        if (!Objects.equals(fileUri.toString(), "")) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        }
        emailIntent.setSelector(selectorIntent);

        source.startActivity(Intent.createChooser(emailIntent, "Select Email App"));
    }
}
