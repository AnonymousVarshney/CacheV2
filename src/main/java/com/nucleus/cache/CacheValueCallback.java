package com.nucleus.cache;

@FunctionalInterface
public interface CacheValueCallback<T> {

	public T getValue(String key);
	
}
