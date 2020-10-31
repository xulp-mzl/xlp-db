package org.xlp.db.sql;


import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * 可含条件单表指定字段的和SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2018-1-7
 *         </p>
 * @version 1.0
 */
public class SumSQL<T> extends QuerySQLAbstract<T> {
	/**
	 * 要求和的字段名称
	 */
	private String sumFieldName;

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param bean
	 * @param sumFieldName
	 *            要求和字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	protected SumSQL(T bean, String sumFieldName) throws EntityException {
		super(bean);
		this.sumFieldName = BeanUtil.getFieldAlias(beanClass, sumFieldName);
	}

	/**
	 * 用beanClass对象构建此对象
	 * 
	 * @param beanClass
	 * @param sumFieldName
	 *            要求和字段名称对应的bean的属性名称
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public SumSQL(Class<T> beanClass, String sumFieldName) throws EntityException {
		super(beanClass);
		this.sumFieldName = BeanUtil.getFieldAlias(beanClass, sumFieldName);
	}

	@Override
	public String getParamSql() {
		String sql = "select sum(" + sumFieldName + ") from " + getTableName()
				+ partSql.toString();
		LOGGER.debug("形成的和查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		String sql = "select sum(" + sumFieldName + ") from " + getTableName()
				+ partSqlToString();
		LOGGER.debug("形成的和查询SQL语句是：" + sql);
		return sql;
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
