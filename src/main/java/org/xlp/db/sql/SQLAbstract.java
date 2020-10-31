package org.xlp.db.sql;


import org.apache.log4j.Logger;
import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * sql抽象类
 * <p>注意：这个类的子类的构造方法中应该先调用父类的构造方法，在执行别的内容，否则可能会抛出异常
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public abstract class SQLAbstract implements SQL{
	//日志记录
	protected final static Logger LOGGER = Logger.getLogger(SQLAbstract.class);
	//bean类型
	protected Class<?> beanClass;
	
	/**
	 * 用bean对象构建此对象
	 * @param beanClass
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public <T> SQLAbstract(T bean) throws EntityException{
		if(bean == null)
			throw new NullPointerException("bean对象必须不为空");
		this.beanClass = bean.getClass();
		if(!BeanUtil.isEntity(beanClass)){
			LOGGER.error(beanClass.getName() + ": 没有XLPEntity实体注解");
			throw new EntityException("此对象不是实体");
		}
		init(bean);
	}
	
	protected SQLAbstract(){}
	
	/**
	 * 初始化数据
	 * 
	 * @param bean
	 * @throws EntityException
	 */
	protected abstract <T> void init(T bean) throws EntityException;
}
