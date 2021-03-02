package com.nucleus.cache;

/**
 * The listener interface for receiving eviction events. The class that is interested in processing
 * a eviction event implements this interface, when the eviction event occurs, that object's
 * onEviction method is invoked.
 *
 * @param <K> the key type
 */
@FunctionalInterface
public interface EvictionListener<K> {

    /**
     * Called on eviction.
     *
     * @param key the key
     */
    void onEviction(K key);
}
