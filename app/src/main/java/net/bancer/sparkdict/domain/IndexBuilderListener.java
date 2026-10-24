package net.bancer.sparkdict.domain;

import net.bancer.sparkdict.domain.utils.DomainException;

/**
 * Receives updates from the index building process. Implementations are
 * responsible for any UI/notification concerns; IndexBuilder itself
 * has no knowledge of Android UI.
 */
public interface IndexBuilderListener {

    /**
     * Receives an indexing progress update.
     *
     * @param indexed number of articles indexed so far
     * @param total   total number of articles to be indexed
     */
    void onProgress(int indexed, int total);

    /**
     * Reports an error that occurred while indexing a dictionary.
     *
     * @param dictionaryName name of the dictionary that could not be indexed
     * @param e              exception that caused the indexing failure
     */
    void onIndexingError(String dictionaryName, DomainException e);

    /**
     * Reports that indexing has completed.
     */
    void onIndexingComplete();
}
