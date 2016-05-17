package com.test.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {
         Class<?> clazz(); //ª∫¥Ê¿‡
      
		String cacheName();//redisª∫¥Ê√˚≥∆
}
