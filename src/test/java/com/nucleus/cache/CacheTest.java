package com.nucleus.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CacheTest {
	
	private GenericCache<Car> cache;
	private int clock = 0;
	Map<String, AtomicInteger> instanceCounter = new HashMap<>();
	
	@Before
	public void init() {
		CacheConfig config = new CacheConfig().setCapacity(3).setIdleTime(5).setMemoryThreshold(2)
				.setLoadFactor((float)0.75);		//creating cache config
		
		CacheValueCallback<Car> valueCallback = new CacheValueCallback<Car>() {
			@Override
			public Car getValue(String key) {
				Car c = new Car(key, "BLACK");
				if(!instanceCounter.containsKey(key)) {
					instanceCounter.put(key, new AtomicInteger(0));
				}
				instanceCounter.get(key).getAndAdd(1);
				return c;
			}
		};

		this.cache = new GenericCacheImpl<Car>(config, valueCallback) {
			@Override
			protected long getCurrentTime() {
				return clock;
			}
		};
	}
	
	@Test
	public void cacheTest_Expiry() {
		String car1RegistrationNumber = "CAR1";
		Assert.assertNull(instanceCounter.get(car1RegistrationNumber));		//initially no counter
		
		
		//Repeated access at no advancement of timer
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 1
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 1
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 1
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 1
		
		clock = 1;		//forwarding the clock to 1 ms
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//still counter = 1
		
		clock = 5;		//forwarding the clock to 5 ms
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//still counter = 1
		
		clock = 11;		//forwarding the clock to 11 ms
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(2, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 2
		
	}
	
	@Test
	public void cacheTest_LRU() {
		String car1RegistrationNumber = "CAR1";
		String car2RegistrationNumber = "CAR2";
		String car3RegistrationNumber = "CAR3";
		
		clock = 1;		//forwarding the clock to 1 ms
		cache.get(car1RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 1
		cache.getStatistics().getStatisticsReport();
		clock = 2;		//forwarding the clock to 2 ms
		cache.get(car2RegistrationNumber);
		cache.get(car2RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car2RegistrationNumber).intValue());	//counter = 1
		cache.getStatistics().getStatisticsReport();
		clock = 3;		//forwarding the clock to 3 ms
		cache.get(car3RegistrationNumber);
		cache.get(car3RegistrationNumber);
		cache.get(car3RegistrationNumber);
		Assert.assertEquals(1, instanceCounter.get(car3RegistrationNumber).intValue());	//counter = 1
		cache.getStatistics().getStatisticsReport();
		String car4RegistrationNumber = "CAR4";
		cache.get(car4RegistrationNumber);			//here the LRU logic should kick in and evict CAR1
		Assert.assertEquals(1, instanceCounter.get(car4RegistrationNumber).intValue());	//counter = 1
		cache.getStatistics().getStatisticsReport();
		cache.get(car1RegistrationNumber);		//recreation of car 1 entry
		Assert.assertEquals(2, instanceCounter.get(car1RegistrationNumber).intValue());	//counter = 2.. yay!!
		cache.getStatistics().getStatisticsReport();
	}

}
