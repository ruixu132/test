package com.test.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class CacheAspject {
	private Logger logger = Logger.getLogger(CacheAspject.class);
	private static final String addCache = "com.test.cache.RedisCache";
	@Resource(name = "baseRedisDao")
	private BaseRedisDao baseRedisDao;
	private Map<String, Object> cacheObj;

	@Around("execution(* com.test.service..*.get*(..))")
	public Object addCache(ProceedingJoinPoint joinPoint) throws Throwable {

		Object obj = null;
		Method me = ((MethodSignature) joinPoint.getSignature()).getMethod(); // 得到被代理类的方法
 
		Annotation[] AS = me.getAnnotations(); // 得到代理方法上的注解类
		cacheObj = new HashedMap();
		for (Annotation annotation : AS) {

			for (Method method : annotation.annotationType().getDeclaredMethods()) {
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				Object invoke = method.invoke(annotation);
				cacheObj.put(method.getName(), invoke);

			}

		}

		String cacheName = (String)cacheObj.get("cacheName");
		String className =   ((Class)cacheObj.get("clazz")).getName();
		logger.info(className+"-----------"+cacheName);
		
		// 取得被代理类的类名
//		String className = joinPoint.getTarget().getClass().getName();
		// String cacheName =
		// 取得代理方法名
//		String methodName = joinPoint.getSignature().getName();
		String redisKey = className+"_"+cacheName;
		// 判断key是否存在在数据库中
		boolean isexits = baseRedisDao.exists(redisKey);

		if (!isexits) { // 如果不存在就查询数据库，反之则返回redis数据
			obj = joinPoint.proceed();
			logger.info(obj);
			baseRedisDao.set(redisKey, obj, 200l);
		} else {
			obj = baseRedisDao.get(redisKey);
		}
		return obj;
	}

	@Around("execution(* com.test.service..*.update*(..))")
	public Object Evict(ProceedingJoinPoint joinPoint) throws Throwable {
		Object obj = null;
		Method me = ((MethodSignature) joinPoint.getSignature()).getMethod(); // 得到被代理类的方法
 
		Annotation[] AS = me.getAnnotations(); // 得到代理方法上的注解类
		cacheObj = new HashedMap();
		for (Annotation annotation : AS) {

			for (Method method : annotation.annotationType().getDeclaredMethods()) {
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				Object invoke = method.invoke(annotation);
				cacheObj.put(method.getName(), invoke);

			}

		}
		String cacheName = (String)cacheObj.get("cacheName");
		String className =    ((Class)cacheObj.get("clazz")).getName();
		String redisKey = className+"_"+cacheName;
		boolean isexits = baseRedisDao.exists(redisKey);
		if(isexits){
			baseRedisDao.remove(redisKey);
		}

		return 		joinPoint.proceed();
	}

}
