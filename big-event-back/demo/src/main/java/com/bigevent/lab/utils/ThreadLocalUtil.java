package com.bigevent.lab.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal工具类
 * 用于在当前请求线程中安全传递用户信息
 */
public class ThreadLocalUtil {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static <T> T get(String key) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            return null;
        }
        return (T) map.get(key);
    }

    public static void set(String key, Object value) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        map.put(key, value);
    }

    public static void set(Map<String, Object> map) {
        THREAD_LOCAL.set(map);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
