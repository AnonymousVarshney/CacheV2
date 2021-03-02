package com.nucleus.cache;

public interface CacheStats {

    /**
     * Number of times cache was hit
     *
     * @return cache hit count
     */
    long getHitCount();

    /**
     * Number of times cache was missed
     *
     * @return cache miss count
     */
    long getMissCount();

    /**
     * Number of times cache was loaded
     *
     * @return cache load count
     */
    long getLoadCount();

    /**
     * Number of times cache was evicted
     *
     * @return eviction count
     */
    long getEvictionCount();

    /**
     * Number of items in cache
     *
     * @return actual count
     */

     long getActualCount();

    /**
     * Number of times cache was requested
     *
     * @return request count
     */
    long getRequestCount();

    /**
     * The hit rate
     *
     * @return hit rate
     */
    double hitRate();

    /**
     * The miss rate
     *
     * @return miss rate
     */
    double missRate();

    void getStatisticsReport();
}
