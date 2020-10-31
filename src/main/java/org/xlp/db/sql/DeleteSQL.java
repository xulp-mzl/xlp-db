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

	@Override
	public String getParamSql() {
		String sql = "delete from " + getTableName() + partSql.toString();
		LOGGER.debug("形成的删除SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		String sql = "delete from " + getTableName() + partSqlToString();
		LOGGER.debug("形成的删除SQL语句是：" + sql);
		return sql;
	}
	
}
