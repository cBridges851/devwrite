package uk.ac.wlv.devwrite.Search;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.devwrite.Models.Post;

/**
 * Class that finds posts based on a given title or content the user has typed into the search.
 */
public class SearchHelper {
    /**
     * Gets the list of posts that match the query (case insensitive)
     * @param query The string the user has inputted into the search
     * @param posts The posts to filter
     * @return The posts that have the text the user inputted within their title or content
     */
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
