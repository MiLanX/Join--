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
	     * ִ������{@link RedisUtil}�ĸ����࣬ 
	     * ����֤��ִ�в���֮���ͷ�����ԴreturnResource(jedis) 
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
	         * �ص� 
	         * @return ִ�н�� 
	         */  
	      abstract T execute();  
	  
	        /** 
	         * ����{@link #execute()}������ִ�н�� 
	         * ����֤��ִ��{@link #execute()}֮���ͷ�����ԴreturnResource(jedis) 
	         * @return ִ�н�� 
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
	     * ɾ��ģ��ƥ���key 
	     * @param likeKey ģ��ƥ���key 
	     * @return ɾ���ɹ������� 
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
	     * ɾ�� 
	     * @param key ƥ���key 
	     * @return ɾ���ɹ������� 
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
	     * ɾ�� 
	     * @param keys ƥ���key�ļ��� 
	     * @return ɾ���ɹ������� 
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
	     * Ϊ���� key ��������ʱ�䣬�� key ����ʱ(����ʱ��Ϊ 0 )�����ᱻ�Զ�ɾ���� 
	     * �� Redis �У���������ʱ��� key ����Ϊ���ɻӷ���(volatile)�ġ� 
	     * @param key 
	     * @param expire �������ڣ���λΪ�� 
	     * @return 1: ���óɹ� 0: �Ѿ���ʱ��key������ 
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
	     * һ����jvm��id��������������redisԭ���Բ������ص� 
	     * @param key id��key 
	     * @return �������ɵ�Id 
	     */  
	    public long makeId(final String key) {  
	        return new Executor<Long>(shardedJedisPool) {  
	  
	            @Override  
	            Long execute() {  
	                long id = jedis.incr(key);  
	                if ((id + 75807) >= Long.MAX_VALUE) {  
	                    // ������������ã�getSet����֮ǰ����incr��ӣ�75807����Ԥ���Ĳ�ӿռ�  
	                    jedis.getSet(key, "0");  
	                }  
	                return id;  
	            }  
	        }.getResult();  
	    }  

}
