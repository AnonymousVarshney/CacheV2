package com.nucleus.cache;

public class CacheConfig {
	
	private int capacity;
	private int idleTime;
	private int memoryThreshold;
	private float loadFactor;
	public int getCapacity() {
		return capacity;
	}
	public CacheConfig setCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}
	public int getIdleTime() {
		return idleTime;
	}
	public CacheConfig setIdleTime(int idleTime) {
		this.idleTime = idleTime;
		return this;
	}

	public int getMemoryThreshold() {
		return memoryThreshold;
	}

	public CacheConfig setMemoryThreshold(int memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
		return this;
	}

	public float getLoadFactor() {
		return loadFactor;
	}

	public CacheConfig setLoadFactor(float loadFactor) {
		this.loadFactor = loadFactor;
		return this;
	}
}
