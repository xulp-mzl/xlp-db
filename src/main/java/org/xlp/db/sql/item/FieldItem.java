package org.xlp.db.sql.item;

import java.util.Arrays;

import org.xlp.db.sql.table.Table;

/**
 * <p>创建时间：2022年5月13日 下午8:00:34</p>
 * @author xlp
 * @version 1.0 
 * @Description sql字段条件类
*/
public class FieldItem {
	/**
	 * 连接符
	 */
	private ConnectorEnum connector;
	
	/**
	 * 操作符
	 */
	private OperatorEnum operator;
	
	/**
	 * 字段名称
	 */
	private String fieldName;
	
	/**
	 * 字段对应的值
	 */
	private Object value;
	
	/**
	 * between或in字段对应的值
	 */
	private Object[] values;
	
	/**
	 * 字段对应的表描述
	 */
	private Table<?> table;
	
	public FieldItem() {
	}

	public FieldItem(OperatorEnum operator) {
		this.operator = operator;
	}
	
	public FieldItem(OperatorEnum operator, Table<?> table) {
		this.operator = operator;
		this.table = table;
	}

	public FieldItem(ConnectorEnum connector, OperatorEnum operator, String fieldName, Object value, Table<?> table) {
		this.connector = connector;
		this.operator = operator;
		this.fieldName = fieldName;
		this.value = value;
		this.table = table;
	}

	public FieldItem(ConnectorEnum connector, OperatorEnum operator, String fieldName, Object[] values,
			Table<?> table) {
		this.connector = connector;
		this.operator = operator;
		this.fieldName = fieldName;
		this.values = values;
		this.table = table;
	}

	public ConnectorEnum getConnector() {
		return connector;
	}

	public void setConnector(ConnectorEnum connector) {
		this.connector = connector;
	}

	public OperatorEnum getOperator() {
		return operator;
	}

	public void setOperator(OperatorEnum operator) {
		this.operator = operator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public Table<?> getTable() {
		return table;
	}

	public void setTable(Table<?> table) {
		this.table = table;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FieldItem [connector=").append(connector).append(", operator=").append(operator)
				.append(", fieldName=").append(fieldName).append(", value=").append(value).append(", values=")
				.append(Arrays.toString(values)).append(", table=").append(table).append("]");
		return builder.toString();
	}
}
