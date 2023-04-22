package uk.ac.wlv.devwrite.Search;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.devwrite.Models.Post;

public class SearchHelper {
    public List<Post> getResults(String query, List<Post> posts) {
        List<Post> results = new ArrayList<>();

        for (Post post : posts) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase())) {
                results.add(post);
            } else if (post.getContent().toLowerCase().contains(query.toLowerCase())) {
                results.add(post);
            }
        }

        return results;
    }
}
