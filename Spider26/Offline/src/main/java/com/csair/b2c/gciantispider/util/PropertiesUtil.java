package com.csair.b2c.gciantispider.util;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by wangsenfeng on 2017/2/23 0023.
 * 获取配置文件及信息的工具类
 */
public class PropertiesUtil {
    private static ResourceLoader loader = ResourceLoader.getInstance();
    private static ConcurrentMap<String, String> configMap = new ConcurrentHashMap<>();
    private static final String DEFAULT_CONFIG_FILE = "jedisConfig.properties";
    private static Properties prop = null;

    /**
     * 通过配置文件名获取配置文件中key对应的value值,如果未设置使用默认值
     */
    public static String getStringByKey(String key, String default_key, String propName) {
        prop = getProperties(propName);
        key = key.trim();
        if (!configMap.containsKey(key)) {
            if (prop.getProperty(key) != null) {
                configMap.put(key, prop.getProperty(key));
            } else if (prop.getProperty(default_key) != null) {
                configMap.put(key, prop.getProperty(default_key));
            }
        }
        return configMap.get(key);
    }

    /**
     * 通过配置文件名获取配置文件中key对应的value值
     */
    public static String getStringByKey(String key, String propName) {
        prop = getProperties(propName);
        key = key.trim();
        if (!configMap.containsKey(key)) {
            if (prop.getProperty(key) != null) {
                configMap.put(key, prop.getProperty(key));
            }
        }
        return configMap.get(key);
    }

    /**
     * 使用默认设置的配置文件名获取配置文件中key对应的value值
     */
    public static String getStringByKey(String key) {
        return getStringByKey(key, DEFAULT_CONFIG_FILE);
    }

    /**
     * 加载默认配置文件
     */
    public static Properties getProperties() {
        try {
            return loader.getPropFromProperties(DEFAULT_CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加载配置文件
     */
    public static Properties getProperties(String propName) {
        try {
            prop = loader.getPropFromProperties(propName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prop;
    }
}
