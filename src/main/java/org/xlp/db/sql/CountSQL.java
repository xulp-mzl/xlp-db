package org.xlp.db.sql;

import org.xlp.db.exception.EntityException;

/**
 * 可含条件单表数据条数统计SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-27
 *         </p>
 * @version 1.0
 */
public class CountSQL<T> extends QuerySQLAbstract<T>{
	private final static String COUNT_STRING = "select count(*) from ";
	
	/**
	 * 用bean对象构建此对象
	 * @param bean
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	protected CountSQL(T bean) throws EntityException {
		super(bean);
	}
	
	/**
	 * 用beanClass对象构建此对象
	 * @param beanClass
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public CountSQL(Class<T> beanClass) throws EntityException{
		super(beanClass);
	}

	@Override
	public String getParamSql() {
		String sql = COUNT_STRING + getTableName() + partSql.toString();
		LOGGER.debug("形成的统计查询SQL语句是：" + sql);
		return  sql;
	}

	@Override
	public String getSql() {
		String sql = COUNT_STRING + getTableName() + partSqlToString();
		LOGGER.debug("形成的统计查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
