package net.bancer.sparkdict;

import androidx.lifecycle.ViewModel;

import net.bancer.sparkdict.domain.core.LexicalEntry;

import java.util.ArrayList;

/**
 * ViewModel to store the search results (articles) across configuration changes.
 */
public class SparkDictViewModel extends ViewModel {

    private final ArrayList<LexicalEntry> articles = new ArrayList<>();

    /**
     * Gets the list of articles.
     *
     * @return the list of articles.
     */
    public ArrayList<LexicalEntry> getArticles() {
        return articles;
    }
}
