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
	
	/**
	 * 获取查询语句的前缀
	 * @param source 是否返回预处理sql语句
	 * 
	 * @return
	 */
	private String preSql(boolean source) {
		String tableAlias = SQLUtil.getTableAlias(getTable());
		StringBuilder sb = new StringBuilder();
		sb.append("select max(").append(tableAlias)
			.append(maxFieldName).append(") from ").append(getTable().getTableName())
			.append(" ")
			.append(tableAlias.isEmpty() ? tableAlias : tableAlias.substring(0, tableAlias.length() - 1))
			.append(" ");
		//拼接条件
		String condition = source ? formatterConditionSourceSql() : formatterConditionSql();
		if (!condition.isEmpty()) {
			sb.append("where ").append(condition);
		}
		//拼接分组排序
		sb.append(formatterGroupByAndOrderBySql());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("形成的和查询SQL语句是：" + sb);
		}
		return sb.toString();
	}

	@Override
	public String getParamSql() {
		return preSql(false);
	}

	@Override
	public String getSql() {
		return preSql(true);
	}

	@Override
	protected void init(T bean) throws EntityException {
	}
}
