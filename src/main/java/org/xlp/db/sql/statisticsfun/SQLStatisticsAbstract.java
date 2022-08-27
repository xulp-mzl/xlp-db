package org.xlp.db.sql.statisticsfun;

import org.xlp.assertion.AssertUtils;

/**
 * <p>创建时间：2022年5月15日 下午5:36:15</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public abstract class SQLStatisticsAbstract implements SQLStatisticsType{
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
	 * @param fieldName
	 * @param alias
	 */
	SQLStatisticsAbstract(String fieldName, String alias) {
		setAlias(alias); 
		setFieldName(fieldName);
	}

	/**
	 * @param fieldName
	 */
	SQLStatisticsAbstract(String fieldName) {
		this(fieldName, null);
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

	@Override
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
	
}
