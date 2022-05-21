package org.xlp.db.sql.statisticsfun;

import org.xlp.assertion.AssertUtils;
import org.xlp.db.sql.SQL;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.sql.table.Table;
import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2022年5月15日 下午5:36:15</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public abstract class SQLStatisticsAbstract implements SQLStatisticsType{
	/**
	 * 表对象
	 */
	private Table<?> table;
	
	/**
	 * 别名
	 */
	private String alias;
	
	/**
	 * 统计的字段名称
	 */
	private String fieldName;
	
	/**
	 * 
	 * @param table
	 * @param fieldName
	 * @param alias
	 */
	SQLStatisticsAbstract(Table<?> table, String fieldName, String alias) {
		setTable(table);
		setAlias(alias); 
		setFieldName(fieldName);
	}

	/**
	 * 
	 * @param table
	 * @param fieldName
	 */
	SQLStatisticsAbstract(Table<?> table, String fieldName) {
		this(table, fieldName, null);
	}
	
	SQLStatisticsAbstract(){}
	
	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Table<?> getTable() {
		return table;
	}

	/**
	 * 设置表
	 * 
	 * @param table
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public void setTable(Table<?> table) {
		AssertUtils.isNotNull(table, "table parameter is null！");
		this.table = table;
	}

	public String getFieldName() {
		return fieldName;
	}

	/**
	 * 设置统计字段
	 * 
	 * @param fieldName
	 * @throws NullPointerException 假如参数为空，则抛出该异常
	 */
	public void setFieldName(String fieldName) {
		AssertUtils.isNotNull(fieldName, "fieldName parameter is null or emppty！");
		this.fieldName = fieldName;
	}
	
	/**
	 * 统计函数的名称
	 * 
	 * @param funcName
	 * @return
	 * @throws NullPointerException 假如统计函数的名称参数为空，则抛出该异常
	 */
	protected String getStatisticsPartSql(String funcName) {
		AssertUtils.isNotNull(funcName, "统计函数的名称参数不能为空！");
		StringBuilder sb = new StringBuilder();
		sb.append(funcName).append(SQL.LEFT_BRACKET)
			.append(SQLUtil.getColumnName(getFieldName(), table))
			.append(SQL.RIGHT_BRACKET);
		if (!XLPStringUtil.isEmpty(getAlias())) {
			sb.append(" ").append(SQLUtil.getColumnName(getAlias()));
		}
		return sb.toString();
	}
}
