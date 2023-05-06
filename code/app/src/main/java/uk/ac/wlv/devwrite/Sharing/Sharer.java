package uk.ac.wlv.devwrite.Sharing;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Sharer {
    public void sharePostToLinkedIn(Fragment source, String content, Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setPackage("com.linkedin.android");
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        
        try {
            source.startActivity(shareIntent);
        } catch (Exception exception) {
            Toast.makeText(source.getActivity(), "LinkedIn is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void sharePostToEmail(Fragment source, String title, String content, Uri fileUri) {
        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        emailIntent.setSelector(selectorIntent);

        source.startActivity(Intent.createChooser(emailIntent, "Select Email App"));
    }
}
