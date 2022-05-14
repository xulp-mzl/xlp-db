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

	/**
	 * 获取查询语句的前缀
	 * @param source 是否返回预处理sql语句
	 * 
	 * @return
	 */
	private String preSql(boolean source) {
		String tableAlias = SQLUtil.getTableAlias(getTable());
		StringBuilder sb = new StringBuilder();
		sb.append(COUNT_STRING).append(getTable().getTableName())
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
