package com.nucleus.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

public class GenericCacheImpl<T> implements GenericCache<T>{
	
	private int capacity;
	private int memoryThreshold;
	private int idleTime;
	private float loadFactor;
	private CacheValueCallback<T> valueCallback;
	private Map<String, ValueWrapper<T>> cache;
	private Map<String, DiskValueWrapper> diskCache;
	private CacheStatsImpl stats;
	
	public GenericCacheImpl(CacheConfig config, CacheValueCallback<T> valueCallback) {
		this.capacity = config.getCapacity();
		this.memoryThreshold=config.getMemoryThreshold();
		this.idleTime = config.getIdleTime();
		this.loadFactor= config.getLoadFactor();
		this.valueCallback = valueCallback;
		this.diskCache = new ConcurrentHashMap<>();
		this.cache = new ConcurrentHashMap<>();
		this.stats=new CacheStatsImpl();
	}
	
	@Override
	public T get(String key) {
		T finalValue = null;
		
		if((!cache.containsKey(key)) && (!diskCache.containsKey(key))) {						//first time access.. remove LRU entries and create value wrapper
			long t1=getCurrentTime();
			finalValue = valueCallback.getValue(key);
			if(cache.size() >= memoryThreshold) {
				removeLRUEntriesAndCopyToDisk();
			}
			if(cache.size()+diskCache.size() >= capacity) {
				removeLRUEntries();
			}
			cache.put(key, new ValueWrapper<T>(finalValue));
			stats.incrementMissCount();
			stats.incrementActualCount();
			long t2=getCurrentTime();
			stats.addReplenishTime(t2-t1);
		}else if(cache.get(key)!=null && cache.get(key).lastAccessTime < (getCurrentTime() - idleTime)) {		//expired value.. re-create value wrapper
			long t1=getCurrentTime();
			finalValue = valueCallback.getValue(key);
			cache.put(key, new ValueWrapper<T>(finalValue));	//replace the wrapper
			stats.incrementLoadCount();
			long t2=getCurrentTime();
			stats.addReplenishTime(t2-t1);
		}else if(cache.get(key)!=null){													//not expired.. update last access time for the value
			finalValue = cache.get(key).value;
			cache.get(key).lastAccessTime = getCurrentTime();
			stats.incrementHitCount();
		}else if(diskCache.get(key)!=null && diskCache.get(key).lastAccessTime < (getCurrentTime() - idleTime)) {
			//replenish actual cache and delete from disk cache if value is found in diskCache
			long t1=getCurrentTime();
			finalValue = valueCallback.getValue(key);
			if(cache.size() >= memoryThreshold) {
				removeLRUEntriesAndCopyToDisk();
			}
			if(cache.size()+diskCache.size() >= capacity) {
				removeLRUEntries();
			}
			cache.put(key, new ValueWrapper<T>(finalValue));
			stats.incrementActualCount();
			long t2=getCurrentTime();
			stats.addReplenishTime(t2-t1);
			Thread diskDeleteThread=new Thread(()->deleteValueFromDisk(key));
			diskDeleteThread.start();
		}else if(diskCache.get(key)!=null){													//not expired.. update last access time for the value
			long t1=getCurrentTime();
			finalValue = readValueFromDisk(key);
			if(cache.size() >= memoryThreshold) {
				removeLRUEntriesAndCopyToDisk();
			}
			if(cache.size()+diskCache.size() >= capacity) {
				removeLRUEntries();
			}
			cache.put(key, new ValueWrapper<T>(finalValue));
			stats.incrementActualCount();
			long t2=getCurrentTime();
			stats.addReplenishTime(t2-t1);
			Thread diskDeleteThread=new Thread(()->deleteValueFromDisk(key));
			diskDeleteThread.start();
		}
		
		return finalValue;
	}
	
	private synchronized void removeLRUEntries() {		//synchronized method
		if(cache.size()+diskCache.size() >= capacity) {
			long t1=getCurrentTime();
			Entry<String, ValueWrapper<T>> eldestEntry = null;
			for(Entry<String, ValueWrapper<T>> entry: cache.entrySet()) {
				if(eldestEntry == null) {
					eldestEntry = entry;
				}else if(entry.getValue().lastAccessTime < eldestEntry.getValue().lastAccessTime) {
					eldestEntry = entry;
				}
			}
			final String key=eldestEntry.getKey();
			final T value=eldestEntry.getValue().value;
			Thread diskCopyThread=new Thread(()->copyValueToDisk(key,value));
			diskCopyThread.start();

            int valuesToEvictFromDisk=Math.round((1-loadFactor)* diskCache.size());
            //Need logic here to remove top n entries by time in ascending order, need to discuss
			Entry<String,DiskValueWrapper> eldestEntryDisk = null;
			for(Entry<String,DiskValueWrapper> entryDisk: diskCache.entrySet()) {
				if(eldestEntryDisk == null) {
					eldestEntryDisk = entryDisk;
				}else if(entryDisk.getValue().lastAccessTime < eldestEntry.getValue().lastAccessTime) {
					eldestEntryDisk = entryDisk;
				}
			}
			if(eldestEntryDisk!=null) {
				final String keyDelete = eldestEntryDisk.getKey();
				Thread diskDeleteThread = new Thread(() -> deleteValueFromDisk(keyDelete));
				diskDeleteThread.setPriority(1);
				diskDeleteThread.start();
			}

			long t2=getCurrentTime();
			stats.addLRUOptimizationTime(t2-t1);
		}
	}

	private synchronized void removeLRUEntriesAndCopyToDisk() {		//synchronized method
		if(cache.size() >= memoryThreshold) {
			long t1=getCurrentTime();
			Entry<String, ValueWrapper<T>> eldestEntry = null;
			for(Entry<String, ValueWrapper<T>> entry: cache.entrySet()) {
				if(eldestEntry == null) {
					eldestEntry = entry;
				}else if(entry.getValue().lastAccessTime < eldestEntry.getValue().lastAccessTime) {
					eldestEntry = entry;
				}
			}
			final String key=eldestEntry.getKey();
			final T value=eldestEntry.getValue().value;
			Thread diskCopyThread=new Thread(()->copyValueToDisk(key,value));
			diskCopyThread.start();
			long t2=getCurrentTime();
			stats.addLRUOptimizationTime(t2-t1);
		}
	}

	private synchronized void copyValueToDisk(String key,T value){


		try {
			FileOutputStream f = new FileOutputStream(new File(key+".txt"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject(value);

			o.close();
			f.close();
			this.diskCache.put(key,new DiskValueWrapper());
			stats.incrementDiskCount();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}
		cache.remove(key);
	}

	private T readValueFromDisk(String key){

		T returnValue=null;
		try {

            FileInputStream fi = new FileInputStream(new File(key+".txt"));
			ObjectInputStream oi = new ObjectInputStream(fi);
			// Read objects from file
			returnValue=(T)oi.readObject();

			oi.close();
			fi.close();
			this.diskCache.remove(key);
			stats.decrementDiskCount();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	private synchronized void deleteValueFromDisk(String key){

		try {
			File file = new File(key+".txt");
			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Sorry, unable to delete the file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(diskCache.get(key)!=null) {  //Need to identify cause
			diskCache.get(key).evictionListener.onEviction(key);
			diskCache.remove(key);
			stats.decrementDiskCount();
		}
	}

	class ValueWrapper<V>{

		V value;
		long lastAccessTime;
		EvictionListener<String> evictionListener;
		
		ValueWrapper(V value) {
			this.value = value;
			this.lastAccessTime = getCurrentTime();
			this.evictionListener= key -> {
				System.out.println("\nValue is being removed with key: "+key);
				stats.incrementEvictionCount();
				stats.decrementActualCount();
			};
		}
	}

	class DiskValueWrapper{


		long lastAccessTime;
		EvictionListener<String> evictionListener;

		DiskValueWrapper() {
			this.lastAccessTime = getCurrentTime();
			this.evictionListener= key -> {
				System.out.println("\nValue is being removed with key: "+key);
				stats.incrementEvictionCount();
				stats.decrementActualCount();
			};
		}
	}

	protected long getCurrentTime() {			//protected method so that it can be overridden in test class
		return System.currentTimeMillis();
	}

	@Override
	public CacheStats getStatistics() {
		return stats;
	}

}
