package org.xlp.db.sql;

import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * 简单sql语句片段形成类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 3.0
 * 
 */
public final class SQLUtil {
	/**
	 * 得到tablename
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <T> String getTableName(Class<T> beanClass) {
		XLPEntity xlpEntity = BeanUtil.getXLPEntity(beanClass);
		if (xlpEntity == null) {
			return null;
		}
		String tableName = xlpEntity.tableName();
		return XLPStringUtil.isNullOrEmpty(tableName) ? null : tableName;
	}

	/**
	 * 形成查询单表的主键最大值查询语句
	 * 
	 * @param beanClass
	 * @param pkName 主键名称
	 * @return
	 */
	public static <T> String max(Class<T> beanClass, String pkName) {
		String tableName = getTableName(beanClass);
		if (tableName == null || pkName == null) {
			return null;
		}
		String sql = "SELECT MAX(" +pkName + ") FROM " + tableName;
		return sql;
	}

	/**
	 * 形成单表的无条件删除语句
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <T> String delete(Class<T> beanClass) {
		String tableName = getTableName(beanClass);
		if (tableName == null) {
			return null;
		}

		StringBuffer deleteSql = new StringBuffer("DELETE FROM ").append(
				tableName).append(" WHERE 1=1 ");

		return deleteSql.toString();
	}
	
	/**
	 * 用给定的参数值填充SQL语句中的问号
	 * 
	 * @param sql 带问号的SQL语句
	 * @param values 填充值
	 * @return 返回填充后的SQL语句
	 * @throws NullPointerException 假如参数sql为null，抛出该异常
	 */
	public static String fillWithParams(StringBuilder sql, Object[] values){
		if (values == null || values.length == 0)
			return sql.toString();
		String sqlString = sql.toString();
		for (int i = 0, len = values.length; i < len; i++) {
			int index = XLPStringUtil.getCharacterPosition(sqlString, SQL.INTERROGATION, 1);
			System.out.println("******index=" + index + "*******i=" + i);
			if(values[i] != null){
				if(values[i] instanceof CharSequence || values[i] instanceof Character)
					sql.replace(index, index + 1, SQL.SINGLE_QUOTE + values[i] + SQL.SINGLE_QUOTE);
				else if (values[i].getClass().equals(Boolean.class))
					if((Boolean)values[i])
						sql.replace(index, index + 1, "1");
					else
						sql.replace(index, index + 1, "0");
				else
					sql.replace(index, index + 1, values[i] + "");
			}else{
				sql.replace(index, index + 1, "null");
			}
			sqlString = sql.toString();
		}
		
		return sql.toString();
	}
}
