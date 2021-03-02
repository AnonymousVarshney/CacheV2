package com.nucleus.cache;

import java.util.concurrent.atomic.LongAdder;

public class CacheStatsImpl implements CacheStats{

    private static final LongAdder hitCount = new LongAdder();
    private static final LongAdder missCount = new LongAdder();
    private static final LongAdder loadCount = new LongAdder();
    private static final LongAdder evictionCount = new LongAdder();
    private static final LongAdder actualCount = new LongAdder();
    private static final LongAdder diskCount = new LongAdder();
    private static final LongAdder replishmentTime = new LongAdder();
    private static final LongAdder lruOptimizationTime = new LongAdder();

    @Override
    public long getHitCount() {
        return hitCount.longValue();
    }

    @Override
    public long getMissCount() {
        return missCount.longValue();
    }

    @Override
    public long getLoadCount() {
        return loadCount.longValue();
    }

    @Override
    public long getEvictionCount() {
        return evictionCount.longValue();
    }

    @Override
    public long getActualCount() {
        return actualCount.longValue();
    }

    @Override
    public long getRequestCount() {
        return hitCount.longValue() + missCount.longValue();
    }


    public long getTotalMemoryCount() {
        return actualCount.longValue() + diskCount.longValue();
    }

    @Override
    public double hitRate() {
        return hitCount.doubleValue() / getRequestCount();
    }

    @Override
    public double missRate() {
        return missCount.doubleValue() / getRequestCount();
    }


    public double averageLRUOptimizationTime() {
        return lruOptimizationTime.doubleValue() / getEvictionCount();
    }


    public double averageValueReplenishTime() {
        return replishmentTime.doubleValue() / (getLoadCount()+getMissCount());
    }


    @Override
    public void getStatisticsReport() {
        System.out.println(new StringBuilder("\nCurrent total cache size (number of entries): ").append(getTotalMemoryCount())
                .append("\nCurrent memory size (number of entries: )").append(actualCount)
                .append("\nCurrent disk size (number of entries): ").append(diskCount)
                .append("\nTotal access count (number of times cache is accessed): ").append(getRequestCount())
                .append("\nHit ratio (% of cache successful hit): ").append(getHitCount())
                .append("\nMiss ratio (% of cache miss that resulted in replenishing of the value): ").append(getMissCount())
                .append("\nAvg LRU optimization time spent in milliseconds: ").append(averageLRUOptimizationTime())
                .append("\nAvg value replenishment time spent in milliseconds: ").append(averageValueReplenishTime())
                .toString());

    }

    /**
     * Increment hit count.
     */
    public void incrementHitCount() {
        hitCount.increment();
    }

    /**
     * Increment miss count.
     */
    public void incrementMissCount() {
        missCount.increment();
    }

    /**
     * Increment load count.
     */
    public void incrementLoadCount() {
        loadCount.increment();
    }

    /**
     * Increment eviction count.
     */
    public void incrementEvictionCount() {
        evictionCount.increment();
    }


    public void incrementActualCount() {
        actualCount.increment();
    }

    public void decrementActualCount() {
        actualCount.decrement();
    }

    public void incrementDiskCount() {
        diskCount.increment();
    }

    public void decrementDiskCount() {
        diskCount.decrement();
    }

    public void addReplenishTime(long time) {
        replishmentTime.add(time);
    }

    public void addLRUOptimizationTime(long time) {
        lruOptimizationTime.add(time);
    }

}
