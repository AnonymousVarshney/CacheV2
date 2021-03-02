package com.nucleus.cache;

public interface GenericCache<T> {

	public T get(String key);

	public CacheStats getStatistics();

}
