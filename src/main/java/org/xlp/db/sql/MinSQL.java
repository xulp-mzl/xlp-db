package org.xlp.db.sql;


import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * 可含条件单表最小值SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2018-1-6
 *         </p>
 * @version 1.0
 */
public class MinSQL<T> extends QuerySQLAbstract<T> {
	/**
	 * 要求的最小值字段名称
	 */
	private String minFieldName;

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param bean
	 * @param minFieldName
	 *            要求最小值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	protected MinSQL(T bean, String minFieldName) throws EntityException {
		super(bean);
		this.minFieldName = BeanUtil.getFieldAlias(beanClass, minFieldName);
	}

	/**
	 * 用beanClass对象构建此对象
	 * 
	 * @param beanClass
	 * @param minFieldName
	 *            要求最小值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public MinSQL(Class<T> beanClass, String minFieldName) throws EntityException {
		super(beanClass);
		this.minFieldName = BeanUtil.getFieldAlias(beanClass, minFieldName);
	}

	@Override
	public String getParamSql() {
		String sql = "select min(" + minFieldName + ") from " + getTableName()
				+ partSql.toString();
		LOGGER.debug("形成的最小值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		String sql = "select min(" + minFieldName + ") from " + getTableName()
				+ partSqlToString();
		LOGGER.debug("形成的最小值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
