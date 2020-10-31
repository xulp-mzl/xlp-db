package org.xlp.db.proxy;

import java.lang.reflect.Method;

import org.xlp.db.tableoption.annotation.XLPTransaction;
import org.xlp.db.utils.XLPDBUtil;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 事务开启代理类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-19
 *         </p>
 * @version 1.0
 * 
 */
public class XLPProxy implements MethodInterceptor{
	/**
	 * 根据要代理的类创建代理对象
	 * 
	 * @param class1 要代理对象的类型
	 * @return 代理对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T createProxy(Class<?> class1){
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(class1.getClassLoader());
		enhancer.setSuperclass(class1);
		enhancer.setCallback(this);
		return (T) enhancer.create();
	}
	
	 /**
	 * 在代理实例上处理方法调用并返回结果
	 * 
	 * @param object
	 *            代理对象
	 * @param method
	 *            被代理的方法
	 * @param params
	 *            该方法的参数数组
	 * @param methodProxy 
	 * 			     代理方法的MethodProxy对象。每个被代理的方法都对应一个MethodProxy对象
	 * @return 方法返回值
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] params,
			MethodProxy methodProxy) throws Throwable {
		if (isOpenTransaction(method)) {//判断该方法是否开启事务
			Object value = null;
			try {
				//开启事务
				XLPDBUtil.beginTransaction();
				
				value = methodProxy.invokeSuper(object, params);
				
				//提交事务
				XLPDBUtil.commitTransaction();
			} catch (Throwable e) {
				//事务回滚
				XLPDBUtil.rollbackTransaction();
				throw e;
			}
			return value;
		}

		//当不是开启事物的方法，直接调用父类的方法
		return methodProxy.invokeSuper(object, params);
	}

	/**
	 * 判断指定的被代理对象指定的方法是否开启了事务
	 * 
	 * @param method 被代理对象的方法
	 * @return
	 */
	private boolean isOpenTransaction(Method method){
		XLPTransaction transaction = method.getAnnotation(XLPTransaction.class);
		return transaction != null;
	}
}
