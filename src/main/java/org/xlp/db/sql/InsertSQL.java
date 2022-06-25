package org.xlp.db.sql;

import java.util.LinkedList;
import java.util.List;

import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.MethodException;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.utils.XLPStringUtil;

/**
 * 插入Insert SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 2.0
 * 
 */
public class InsertSQL extends SQLAbstract {
	// 插入SQL前缀
	public final static String INSERT_INTO = "insert into ";
	// 表名
	private String tableName;
	// 列名集合
	private List<String> columnNames;
	// 对应的值
	private List<Object> values;
	// 主键信息对象
	private CompoundPrimaryKey primaryKey;
	
	/**
	 * 表对象
	 */
	private Table<?> table;

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param beanClass
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> InsertSQL(T bean) throws EntityException {
		super(bean);
	}

	protected InsertSQL() {
	};

	/***
	 * 获取InsertSQL对象
	 * 
	 * @param bean
	 *            实体对象
	 * @return
	 * @throws EntityException
	 *             假如不是实体，则抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，则抛出该异常
	 */
	public static <T> InsertSQL getInstance(T bean)
			throws EntityException {
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) bean.getClass();
		InsertSQL insertSQL = new InsertSQL();
		insertSQL.primaryKey = new CompoundPrimaryKey(bean);
		insertSQL.tableName = SQLUtil.getTableName(beanClass);
		insertSQL.beanClass = beanClass;
		insertSQL.columnNames = new LinkedList<String>();
		insertSQL.values = new LinkedList<Object>();
		return insertSQL;
	}

	/**
	 * 数据初始化
	 * 
	 * @param bean
	 * @throws EntityException
	 */
	@SuppressWarnings("all")
	@Override
	protected <T> void init(T bean) throws EntityException {
		table = new Table<>(bean.getClass());
		this.primaryKey = new CompoundPrimaryKey(bean);
		// 含有XLPColumn注解属性描述数组
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor(
				beanClass).getPdsWithAnnotation(XLPColumn.class);

		int len = pds.length;
		int count = primaryKey.getCount();
		this.tableName = SQLUtil.getTableName(beanClass);
		columnNames = new LinkedList<String>();
		values = new LinkedList<Object>();
		for (int i = 0; i < count; i++) {
			columnNames.add(primaryKey.getNames()[i]);
			values.add(primaryKey.getCurrentValues()[i]);
		}
		XLPColumn xlpColumn;
		Class<?> filedTypeClass;
		String columnName;
		for (int j = 0; j < len; j++) {
			xlpColumn = pds[j].getFieldAnnotation(XLPColumn.class);
			columnName = xlpColumn.columnName();
			columnName = XLPStringUtil.isEmpty(columnName) ? pds[j].getFieldName() : columnName;
			columnNames.add(columnName);
			filedTypeClass = pds[j].getFiledClassType();
			Object value = getValue(pds[j], bean);
			if(String.class.equals(filedTypeClass) && value != null){
				if (xlpColumn.trim()) {
					value = ((String)value).trim();
				}
				int maxLen = xlpColumn.maxLength();
				if (maxLen != NO_STR_VALUE_MAX_LEN) {
					value = XLPStringUtil.getSuitLenString((String)value, maxLen);
				}
				// 处理枚举类型
			} else if (filedTypeClass.isEnum() && value != null) {
				value = value.toString();
			}
			values.add(value);
		}
	}

	/**
	 * 获取对应的属性值
	 * 
	 * @param pd
	 * @param bean
	 * @return
	 */
	private <T> Object getValue(PropertyDescriptor<T> pd, T bean) {
		try {
			return pd.executeReadMethod(bean);
		} catch (MethodException e) {
			LOGGER.warn("获取[" + pd.getFieldName() + "]该属性值失败！");
		}
		return null;
	}

	/**
	 * 得到要操作的表名
	 * 
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 得到要插入的列数组
	 * 
	 * @return
	 */
	public String[] getColumnNames() {
		return columnNames.toArray(new String[0]);
	}

	/**
	 * 得到要插入的值数组
	 * 
	 * @return
	 */
	public Object[] getValues() {
		return values.toArray();
	}

	/**
	 * 得到主键信息
	 * 
	 * @return
	 */
	public CompoundPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * 添加要添加的列
	 * 
	 * @param fieldName
	 *            可以是实体的字段名，也可以数据库中的列名称
	 * @param value
	 *            对应的值
	 * @return
	 */
	public InsertSQL insert(String fieldName, Object value) {
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
			columnNames.add(colName);
			values.add(value);
		}
		return this;
	}

	/**
	 * 清除所有要添加的列
	 * 
	 * @return
	 */
	public InsertSQL clearInsert() {
		columnNames.clear();
		values.clear();
		return this;
	}

	/**
	 * 清除指定的添加列
	 * 
	 * @param fieldName
	 *            可以是实体的字段名，也可以数据库中的列名称
	 * @return
	 */
	public InsertSQL removeInsert(String fieldName) {
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
			int size = columnNames.size();
			for (int i = 0; i < size; i++) {
				if(colName.equals(columnNames.get(i))){
					columnNames.remove(i);
					values.remove(i);
					break;
				}
			}
		}
		return this;
	}

	/**
	 * 返回不带参数插入语句
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		String sql = getSql();
		if (sql == null)
			sql = "insert into " + tableName + "() values()";
		LOGGER.info("形成的插入SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getParamSql() {
		int len = columnNames.size();
		if (len == 0)
			return null;
		StringBuilder sb = new StringBuilder(INSERT_INTO).append(tableName)
				.append(LEFT_BRACKET);
		StringBuilder valueSb = new StringBuilder("values")
				.append(LEFT_BRACKET);// value部分
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				sb.append(COMMA);
				valueSb.append(COMMA);
			}
			sb.append(columnNames.get(i));
			valueSb.append(INTERROGATION);
		}
		sb.append(RIGHT_BRACKET);
		valueSb.append(RIGHT_BRACKET);
		sb.append(valueSb);

		String sql = sb.toString();
		LOGGER.debug("形成的插入SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		int len = columnNames.size();
		if (len == 0)
			return null;
		StringBuilder sb = new StringBuilder(INSERT_INTO).append(tableName)
				.append(LEFT_BRACKET);
		StringBuilder valueSb = new StringBuilder("values")
				.append(LEFT_BRACKET);// value部分
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				sb.append(COMMA);
				valueSb.append(COMMA);
			}
			sb.append(columnNames.get(i));
			if (values.get(i) == null)
				valueSb.append("null");
			else if (values.get(i) instanceof CharSequence
					|| values.get(i) instanceof Character)
				valueSb.append(SINGLE_QUOTE).append(values.get(i))
						.append(SINGLE_QUOTE);
			else if (values.get(i).getClass().equals(Boolean.class))
				if ((Boolean) values.get(i))
					valueSb.append(1);
				else
					valueSb.append(0);
			else
				valueSb.append(values.get(i));
		}

		sb.append(RIGHT_BRACKET);
		valueSb.append(RIGHT_BRACKET);
		sb.append(valueSb);

		String sql = sb.toString();
		LOGGER.debug("形成的插入SQL语句是：" + sql);
		return sql;
	}

	@Override
	public Object[] getParams() {
		return getValues();
	}

	@Override
	public Class<?> getEntityClass() {
		return beanClass;
	}

	/**
	 * @return the table
	 */
	public Table<?> getTable() {
		return table;
	}
}
