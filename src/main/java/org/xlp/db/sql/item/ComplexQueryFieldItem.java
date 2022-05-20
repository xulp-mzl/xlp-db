package org.xlp.db.sql.item;

import org.xlp.db.sql.table.Table;

/**
 * <p>创建时间：2022年5月19日 上午10:56:06</p>
 * @author xlp
 * @version 1.0 
 * @Description SQL条件对象
*/
public class ComplexQueryFieldItem extends FieldItem{
	public static enum ValueType{
		/**
		 * 值类型
		 */
		VALUE,
		/**
		 * 字段名称类型
		 */
		FIELD,
		/**
		 * SQL对象类型
		 */
		SQL
	}

	/**
	 * 标记条件值得类型
	 */
	private ValueType valueType = ValueType.VALUE;
	
	public ComplexQueryFieldItem() {
		super();
	}

	public ComplexQueryFieldItem(ConnectorEnum connector, OperatorEnum operator, String fieldName, Object value,
			Table<?> table) {
		super(connector, operator, fieldName, value, table);
	}

	public ComplexQueryFieldItem(ConnectorEnum connector, OperatorEnum operator, String fieldName, Object[] values,
			Table<?> table) {
		super(connector, operator, fieldName, values, table);
	}

	public ComplexQueryFieldItem(OperatorEnum operator, Table<?> table) {
		super(operator, table);
	}

	public ComplexQueryFieldItem(OperatorEnum operator) {
		super(operator);
	}
	
	public ComplexQueryFieldItem(ConnectorEnum connector, OperatorEnum operator, ValueType valueType,
			Object value, Table<?> table) {
		super(connector, operator, null, value, table);
		setValueType(valueType);
	}
	
	public ComplexQueryFieldItem(ConnectorEnum connector, OperatorEnum operator, String fieldName, Object value,
			Table<?> table, ValueType valueType) {
		super(connector, operator, fieldName, value, table);
		setValueType(valueType);
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

}
