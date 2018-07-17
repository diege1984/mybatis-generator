package org.mybatis.generator.internal.util;

import java.lang.reflect.Method;
import java.util.List;

public class RefectUtils {

	public static Method[] getOwnMethod(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		
		
		
		for (Method method : methods) {
			
		}

		return methods;
	}
	
	public static List<Method> getParentMethods(Class<?> clazz){
		//clazz.getsuper
		return null;
	}

	public static void main(String[] args) {
		getOwnMethod(RefectUtils.class);
	}

}
