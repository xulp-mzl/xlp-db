package org.xlp.db.factory;

import org.xlp.db.proxy.XLPProxy;

/**
 * 事务开启代理对象实例化工厂类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-19
 *         </p>
 * @version 1.0
 * 
 */
public final class XLPFactory {
	/**
	 * 获取代理对象
	 * 
	 * @param class1
	 * @return
	 */
	public static <T> T create(Class<?> class1){
		return new XLPProxy().createProxy(class1);
	}
	
	/**
	 * 获取代理对象
	 * 
	 * @param object
	 * @return
	 */
	public static <T> T create(T object){
		return new XLPProxy().createProxy(object.getClass());
	}
}
