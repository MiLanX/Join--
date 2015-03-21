package Utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisUtil {

	private ShardedJedisPool shardedJedisPool;
	 /** 
	     * 执行器，{@link RedisUtil}的辅助类， 
	     * 它保证在执行操作之后释放数据源returnResource(jedis) 
	     * @version V1.0 
	     * @author fengjc 
	     * @param <T> 
	     */  
	    abstract class Executor<T> {  
	  
	        ShardedJedis jedis = null;  
	        ShardedJedisPool shardedJedisPool = null;  
	  
	        public Executor(ShardedJedisPool shardedJedisPool) {  
	            this.shardedJedisPool = shardedJedisPool;  
	            jedis = this.shardedJedisPool.getResource();  
	        }  
	  
	        /** 
	         * 回调 
	         * @return 执行结果 
	         */  
	      abstract T execute();  
	  
	        /** 
	         * 调用{@link #execute()}并返回执行结果 
	         * 它保证在执行{@link #execute()}之后释放数据源returnResource(jedis) 
	         * @return 执行结果 
	         */  
	        public T getResult() {  
	            T result = null;  
	            try {  
	                result = execute();  
	            } catch (Throwable e) {  
	                throw new RuntimeException("Redis execute exception", e);  
	            } finally {  
	                if (jedis != null) {  
	                    shardedJedisPool.returnResource(jedis);  
	                }  
	            }  
	            return result;  
	        }  
	    }  
	  
	    /** 
	     * 删除模糊匹配的key 
	     * @param likeKey 模糊匹配的key 
	     * @return 删除成功的条数 
	     */  
	    public long delKeysLike(final String likeKey) {  
	        return new Executor<Long>(shardedJedisPool) {  
	  
	            @Override  
	            Long execute() {  
	                Collection<Jedis> jedisC = jedis.getAllShards();  
	                Iterator<Jedis> iter = jedisC.iterator();  
	                long count = 0;  
	                while (iter.hasNext()) {  
	                    Jedis _jedis = iter.next();  
	                    Set<String> keys = _jedis.keys(likeKey + "*");  
	                    count += _jedis.del(keys.toArray(new String[keys.size()]));  
	                }  
	                return count;  
	            }  
	        }.getResult();  
	    }  
	  
	    /** 
	     * 删除 
	     * @param key 匹配的key 
	     * @return 删除成功的条数 
	     */  
	    public Long delKey(final String key) {  
	        return new Executor<Long>(shardedJedisPool) {  
	  
	            @Override  
	            Long execute() {  
	                return jedis.del(key);  
	            }  
	        }.getResult();  
	    }  
	  
	    /** 
	     * 删除 
	     * @param keys 匹配的key的集合 
	     * @return 删除成功的条数 
	     */  
	    public Long delKeys(final String[] keys) {  
	        return new Executor<Long>(shardedJedisPool) {  

	            @Override  
	            Long execute() {  
	                Collection<Jedis> jedisC = jedis.getAllShards();  
	               Iterator<Jedis> iter = jedisC.iterator();  
              long count = 0;  
	                while (iter.hasNext()) {  
	                    Jedis _jedis = iter.next();  
	                   count += _jedis.del(keys);  
	                }  
	                return count;  
	           }  
	        }.getResult();  
	    }  
	  
	    /** 
	     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。 
	     * 在 Redis 中，带有生存时间的 key 被称为『可挥发』(volatile)的。 
	     * @param key 
	     * @param expire 生命周期，单位为秒 
	     * @return 1: 设置成功 0: 已经超时或key不存在 
	     */  
	    public Long expire(final String key, final int expire) {  
	        return new Executor<Long>(shardedJedisPool) {  
	  
	            @Override  
	            Long execute() {  
	                return jedis.expire(key, expire);  
	            }  
	        }.getResult();  
	    }  
	  
	    /** 
	     * 一个跨jvm的id生成器，利用了redis原子性操作的特点 
	     * @param key id的key 
	     * @return 返回生成的Id 
	     */  
	    public long makeId(final String key) {  
	        return new Executor<Long>(shardedJedisPool) {  
	  
	            @Override  
	            Long execute() {  
	                long id = jedis.incr(key);  
	                if ((id + 75807) >= Long.MAX_VALUE) {  
	                    // 避免溢出，重置，getSet命令之前允许incr插队，75807就是预留的插队空间  
	                    jedis.getSet(key, "0");  
	                }  
	                return id;  
	            }  
	        }.getResult();  
	    }  

}
