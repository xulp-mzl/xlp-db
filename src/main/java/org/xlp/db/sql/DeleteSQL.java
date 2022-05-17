package org.xlp.db.sql;

import org.xlp.db.exception.EntityException;


/**
 * 可含条件单表数据删除SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-07
 *         </p>
 * @version 1.0
 */
public class DeleteSQL<T> extends OneTableSQLAbstract<T>{
	
	public DeleteSQL(Class<T> beanClass) throws EntityException {
		super(beanClass);
	}

	public DeleteSQL(T bean) throws EntityException {
		super(bean);
	}

	@Override
	protected void init(T bean) throws EntityException {
		int count = primaryKey.getCount();
		String[] keyNames = primaryKey.getNames();
		Object[] keyValues = primaryKey.getValues();
		for (int i = 0; i < count; i++) {
			if (keyValues[i] != null && !(primaryKey.isPrimitives()[i]
				&& "0".equals(keyValues[i].toString()))) {
				andEq(keyNames[i], keyValues[i]);
			}
		}
	}

	/**
	 * 获取查询语句的前缀
	 * @param source 是否返回预处理sql语句
	 * 
	 * @return
	 */
	private String preSql(boolean source) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(getTable().getTableName()).append(" ");
		setTableAlias(null);
		//拼接条件
		String condition = source ? formatterConditionSourceSql() : formatterConditionSql();
		if (!condition.isEmpty()) {
			sb.append("where ").append(condition);
		}
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
	
}
