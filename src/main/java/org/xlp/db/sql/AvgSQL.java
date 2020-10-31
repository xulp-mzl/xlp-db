package org.xlp.db.sql;


import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * 可含条件单表指定字段的平均值SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2018-1-7
 *         </p>
 * @version 1.0
 */
public class AvgSQL<T> extends QuerySQLAbstract<T> {
	/**
	 * 要求的平均值字段名称
	 */
	private String avgFieldName;

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param bean
	 * @param avgFieldName
	 *            要求平均值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	protected AvgSQL(T bean, String avgFieldName) throws EntityException {
		super(bean);
		this.avgFieldName = BeanUtil.getFieldAlias(beanClass, avgFieldName);
	}

	/**
	 * 用beanClass对象构建此对象
	 * 
	 * @param beanClass
	 * @param avgFieldName
	 *            要求平均值字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public AvgSQL(Class<T> beanClass, String avgFieldName) throws EntityException {
		super(beanClass);
		this.avgFieldName = BeanUtil.getFieldAlias(beanClass, avgFieldName);
	}

	@Override
	public String getParamSql() {
		String sql = "select avg(" + avgFieldName + ") from " + getTableName()
				+ partSql.toString();
		LOGGER.debug("形成的平均值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		String sql = "select avg(" + avgFieldName + ") from " + getTableName()
				+ partSqlToString();
		LOGGER.debug("形成的平均值查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
