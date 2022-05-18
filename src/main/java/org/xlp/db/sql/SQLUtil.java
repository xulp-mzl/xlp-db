package org.xlp.db.sql;

import org.xlp.assertion.AssertUtils;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPOutputInfoUtil;
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
			XLPOutputInfoUtil.println("******index=" + index + "*******i=" + i);
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
	
	/**
	 * 获取(?,?,?)格式的sql片段
	 * 
	 * @param length
	 * @return
	 */
	static String formatInSql(int length){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(SQL.LEFT_BRACKET).append(SQL.INTERROGATION);
			}
			sb.append(SQL.COMMA).append(SQL.INTERROGATION);
		}
		if (length > 0) {
			sb.append(SQL.RIGHT_BRACKET);
		}
		return sb.toString();
 	}
	
	/**
	 * 获取table别名
	 * @param table
	 * @return 格式为""或xxx.
	 */
	public static String getTableAlias(Table<?> table){
		String tableAlias = table == null ? XLPStringUtil.EMPTY : table.getAlias();
		tableAlias = XLPStringUtil.isEmpty(tableAlias) ? XLPStringUtil.EMPTY : tableAlias + ".";
		return tableAlias;
	}
	
	/**
	 * 获取指定字段名称对应表中的字段名称 name->表别名+'.'+表字段名， xx.name->'xx.'+表字段名
	 * 
	 * @param fieldName
	 * @param table
	 * @return
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public static String getColumnName(String fieldName, Table<?> table){
		AssertUtils.isNotNull(fieldName, "fieldName parameter is null or empty！");
		AssertUtils.isNotNull(table, "table parameter is null！");
		int index = fieldName.indexOf(".");
		String pre = table.getAlias();
		String suffix = fieldName;
		if (index >= 0) {
			pre = fieldName.substring(0, index + 1);
			suffix = fieldName.substring(index + 1);
		}
		pre = XLPStringUtil.isEmpty(pre) ? XLPStringUtil.EMPTY : pre + ".";
		String colName = BeanUtil.getFieldAlias(table.getEntityClass(), suffix);
		colName = XLPStringUtil.isEmpty(colName) ? suffix : colName;
		return pre + colName;
	}
}
