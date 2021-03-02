# CacheV2
All previous requirement for cache expiry, LRU, callback etc. remain as it is.
Additional requirements
1.       Cache should only store Serializable (java.io.Serializable) objects.
2.       Have additional configuration property of “memory threshold size” in cache configuration beyond which the cache should start storing serialized values on the disk (keeping MRU and LRU access order in mind).
3.       The serialization and storage of cache entry to the disk should be “asynchronous”. Reading back from the disk should be synchronous. Deletion of disk entries should be “low priority asynchronous”.
4.       There should be provision of “listening” the cache entry eviction. By default eviction should be logged on System.out.println if no listener is attached to the cache.
5.       Cache should have a method called “getStatistics()” which returns a Pojo with statistics of cache performance. The following should be published in statistics
a.       Current total cache size (number of entries)
b.       Current memory size (number of entries)
c.       Current disk size (number of entries)
d.       Total access count (number of times cache is accessed)
e.       Hit ratio (% of cache successful hit)
f.        Miss ratio (% of cache miss that resulted in replenishing of the value)
g.       Avg LRU optimization time spent in milliseconds
h.       Avg value replenishment time spent in milliseconds
6.       Functional programming, stream processing and lambda expressions should be used as much as possible.
7.       Junit test cases should be created for making sure that our cache is working as expected.
