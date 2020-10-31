package org.xlp.db.sql;


import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * 可含条件单表最大值SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2018-1-3
 *         </p>
 * @version 1.0
 */
public class MaxSQL<T> extends QuerySQLAbstract<T> {
	/**
	 * 要求的最大值字段名称
	 */
	private String maxFieldName;

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param bean
	 * @param maxFieldName
	 *            要求最大值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	protected MaxSQL(T bean, String maxFieldName) throws EntityException {
		super(bean);
		this.maxFieldName = BeanUtil.getFieldAlias(beanClass, maxFieldName);
	}

	/**
	 * 用beanClass对象构建此对象
	 * 
	 * @param beanClass
	 * @param maxFieldName
	 *            要求最大值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public MaxSQL(Class<T> beanClass, String maxFieldName) throws EntityException {
		super(beanClass);
		this.maxFieldName = BeanUtil.getFieldAlias(beanClass, maxFieldName);
	}

	@Override
	public String getParamSql() {
		String sql = "select max(" + maxFieldName + ") from " + getTableName()
				+ partSql.toString();
		LOGGER.debug("形成的最大值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		String sql = "select max(" + maxFieldName + ") from " + getTableName()
				+ partSqlToString();
		LOGGER.debug("形成的最大值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
