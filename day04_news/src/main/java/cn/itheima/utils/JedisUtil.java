package cn.itheima.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {
    private static JedisPool jedisPool;

    static {
        //创建连接池配置对象
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);//最大连接数
        config.setMaxWaitMillis(30000);//最长等待时间
        config.setMinIdle(5);//最小空闲数
        config.setTestOnBorrow(true);//每次获取到连接后是否测试连接有效
        //创建连接池对象
        jedisPool=new JedisPool(config,"192.168.72.102",6379);

    }
    public Jedis getJedis(){
        return jedisPool.getResource();
    }
}
