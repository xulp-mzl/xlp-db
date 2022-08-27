package org.xlp.db.sql.item;

/**
 * <p>
 * 创建时间：2022年8月27日 下午12:58:15
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 查询列属性
 */
public class QueryColumnProperty {
	/**
	 * bean字段名称
	 */
	private String fieldName;

	/**
	 * 别名
	 */
	private String alias;
	
	/**
	 * 自定义查询值
	 */
	private Object customValue;

	/**
	 * 查询列属性类型
	 */
	private QueryColumnPropertyType queryColumnPropertyType;
	
	public QueryColumnProperty(){}

	public QueryColumnProperty(String fieldName, String alias) {
		this.fieldName = fieldName;
		this.alias = alias;
		this.queryColumnPropertyType = QueryColumnPropertyType.FIELD;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public QueryColumnPropertyType getQueryColumnPropertyType() {
		return queryColumnPropertyType;
	}

	public void setQueryColumnPropertyType(QueryColumnPropertyType queryColumnPropertyType) {
		this.queryColumnPropertyType = queryColumnPropertyType;
	}

	public Object getCustomValue() {
		return customValue;
	}

	/**
	 * @param 获取自定义在值
	 */
	public void setCustomValue(Object customValue) {
		this.customValue = customValue;
	}

	public static enum QueryColumnPropertyType {
		/**
		 * 自定义列名，数据库中不存在，即常数列
		 */
		CUSTOM,
		/**
		 * bean字段查询
		 */
		FIELD
	}
}
