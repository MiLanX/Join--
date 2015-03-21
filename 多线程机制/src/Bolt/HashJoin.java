package Bolt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class HashJoin extends BaseBasicBolt {
	int count;
	int num;
	private Jedis jedis;
	private Pipeline pipe;
//	public static JedisPool pool;
	public static String REDIS_HOST = "10.20.100.5";
	private static void createPool(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(500);
		
		config.setMaxIdle(200);
		config.setMaxWait(100);
		config.setTestOnBorrow(true);
		
		//pool = new JedisPool(config,REDIS_HOST,6379);
	}
//	public static JedisPool getPool(){
//		if(pool == null){
//			createPool();
//		}
//		return pool;
//	}
//	public static Jedis getJedis(){
//		Jedis jedis = null;
//		jedis = getPool().getResource();
//		if(jedis == null){
//			new Jedis(REDIS_HOST);
//		}
//		return jedis;
//		
//	}
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		super.prepare(stormConf, context);
		jedis = new Jedis(REDIS_HOST,6379);
		pipe = jedis.pipelined();
	}

	private String selectDB(Tuple tuple) {
		String id = tuple.getSourceStreamId();
		return id;
	}

	private int getDb(String id) {
		if (id.equals("data1")) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * here is creating the key of redis
	 * 
	 * @param tuple
	 * @return
	 */
	private String getKey(Tuple tuple) {
		String key = tuple.getString(0);
		Long timeStamp = System.currentTimeMillis();
		key = key + "-" + timeStamp;
		return key;
	}

	/**
	 * this method needs to be change the byte[] do the flag
	 * 为了提高存储的效率，我觉得应该Map<key+timeStamp,value>的形式存放， 这样域名就变成了key+timeStamp
	 * 
	 * @param tuple
	 */
	private Map<String, String> getMap(Tuple tuple) {
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("attr1", tuple.getString(1));
		hashMap.put("attr2", tuple.getString(2));
		return hashMap;
	}

	/**
	 * 
	 * @param tuple
	 * @param index
	 */

	private void hashSaveRedis(Tuple tuple, int index,Pipeline pipe) {
        Map<String,String> map = getMap(tuple);
        String key = getKey(tuple);
		String id = tuple.getSourceStreamId();
	    
		if (id.equals("data1")) {
			pipe.select(index);
			pipe.hmset(key, map);
			pipe.expire(key, 10);

		} else if (id.equals("data2")) {
			pipe.select(index);
			pipe.hmset(key, map);
            pipe.expire(key, 10);
			
		} 
		
	}

	

	/**
	 * 这个方法返回与之相对应的数组key
	 * 
	 * @param key
	 * @return
	 */
	private Set<String> matchKeys(String key,int index,Jedis jedis) {
		/**
		 * problem:为什么会出现类型转换错误?这是一个小bug
		 */
	
		jedis.select(index);
        Set<String> keys = null;
		List<String> join = null;
		//pipe.sync();
	    keys = jedis.keys(key +"-"+ "*");
		return keys;
	
	}

	private int select(String id) {
		if (id.equals("data1")) {
			return 1;
		} else
			return 0;

	}
	

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		if (isTickTuple(tuple)) {
			System.err.println("count=" + count);
			count = 0;
		} else {
			count++;
			/**
			 * 这里将value存储到redis中、采用的是hash
			 */
//			Jedis jedis = null;
//		    jedis = getJedis();
//		    Pipeline pipeline = jedis.pipelined();
		    
			hashSaveRedis(tuple, getDb(selectDB(tuple)),pipe);
			 // 取到key
			// matchKeyAndSeek这个方法是对key进行匹配
			pipe.sync();
			 Set<String> keys = matchKeys(tuple.getString(0),select(tuple.getSourceStreamId()),jedis);
			// String join = tuple.getValues().toString();
			 Iterator<String> it = keys.iterator();
			 
//			 Jedis jedis = null;
//			 jedis = getJedis();
			 
			 while(it.hasNext()){
				 String key = it.next();
				 String attribute = "attr0"+"attr1"+"atter2";
			     String result = attribute+tuple.getValues().toString() +jedis.hmget(key, "attr1","attr2").toString();
				collector.emit(new Values(result));
				 
			 }
//			 if(jedis != null){
//				 getPool().returnResource(jedis);
//			 }

			
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("joinResult"));
	}

	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 1);
		return conf;
	}
	
	public static boolean isTickTuple(Tuple tuple) {

		return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
		&& tuple.getSourceStreamId().equals(
		Constants.SYSTEM_TICK_STREAM_ID);
	}
	

	


}

class Multi_Thread extends Thread{
	
	Thread saveRedis,joinRedis;
	Jedis jedis;
	Pipeline pipe;
	Multi_Thread(){
		saveRedis = new Thread(this);
		saveRedis.setName("saveRedis");
		joinRedis = new Thread(this);
		joinRedis.setName("joinReids");
	    jedis = new Jedis("localhost");
		pipe = jedis.pipelined();
	}
	@Override
	public void run() {
		while(true){
			//如果这个线程是存数据的线程的话
			if(Thread.currentThread() == saveRedis){
				
			}
			//如果这个数据是取数据的线程的话
			if(Thread.currentThread() == joinRedis){
				
			}
		}
	}
}


























