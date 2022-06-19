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
		return XLPStringUtil.isEmpty(tableName) ? null : tableName;
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
		StringBuilder sb = new StringBuilder(SQL.LEFT_BRACKET);
		for (int i = 0; i < length; i++) {
			if (i != 0) {
				sb.append(SQL.COMMA);
			}
			sb.append(SQL.INTERROGATION);
		}
		sb.append(SQL.RIGHT_BRACKET);
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
	 * @param defaultAddTableAlias 是否加上表的别名做前缀
	 * @return
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public static String getColumnName(String fieldName, Table<?> table, boolean defaultAddTableAlias){
		AssertUtils.isNotNull(fieldName, "fieldName parameter is null or empty！");
		AssertUtils.isNotNull(table, "table parameter is null！");
		int index = fieldName.indexOf(".");
		String pre = defaultAddTableAlias ? table.getAlias() : XLPStringUtil.EMPTY;
		String suffix = fieldName;
		if (index >= 0) {
			pre = fieldName.substring(0, index);
			suffix = fieldName.substring(index + 1);
		}
		pre = XLPStringUtil.isEmpty(pre) ? XLPStringUtil.EMPTY : pre + ".";
		String colName = BeanUtil.getFieldAlias(table, suffix);
		return  pre + colName;
	}
	
	/**
	 * 获取指定字段名称对应表中的字段名称 name->表字段名， xx.name->'xx.'+表字段名
	 * 
	 * @param fieldName
	 * @param table 
	 * @return
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public static String getColumnName(String fieldName, Table<?> table){
		return getColumnName(fieldName, table, true);
	}
	
	/**
	 * 去掉非法字符
	 * 
	 * @param columnName
	 * @return
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public static String getColumnName(String columnName){
		AssertUtils.isNotNull(columnName, "columnName parameter is null or empty!");
		return columnName.replaceAll("\\s", XLPStringUtil.EMPTY)
			.replace("'", XLPStringUtil.EMPTY)
			.replace("--", XLPStringUtil.EMPTY)
			.replace("\\", XLPStringUtil.EMPTY);
	}
}
