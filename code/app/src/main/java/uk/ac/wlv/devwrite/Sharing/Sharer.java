package uk.ac.wlv.devwrite.Sharing;

import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Sharer {
    public void sharePostToLinkedIn(Fragment source, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setPackage("com.linkedin.android");
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        
        try {
            source.startActivity(shareIntent);
        } catch (Exception exception) {
            Toast.makeText(source.getActivity(), "LinkedIn is not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
