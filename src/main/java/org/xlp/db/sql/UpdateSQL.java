package org.xlp.db.sql;

import java.util.ArrayList;
import java.util.List;

import org.xlp.db.exception.EntityException;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.utils.XLPStringUtil;

/**
 * 可含条件单表数据更新SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-07
 *         </p>
 * @version 1.0
 */
public class UpdateSQL<T> extends OneTableSQLAbstract<T>{
	//标记数据更新时是否更新整个Javabean
	private boolean allUpdate = false;
	//要更改的字段名称集合
	private List<String> columnNames = new ArrayList<String>();
	//对应值
	private List<Object> updateValues = new ArrayList<Object>();
	
	public UpdateSQL(T bean) throws EntityException  {
		this(bean, true);
	}
	
	public UpdateSQL(T bean, boolean allUpdate) throws EntityException  {
		super(bean);
		this.allUpdate = allUpdate;
		
		init0(bean);
	}

	public UpdateSQL(Class<T> beanClass) throws EntityException {
		super(beanClass);
	}

	protected UpdateSQL(){}
	
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
	public static <T> UpdateSQL<T> getInstance(T bean)
			throws EntityException {
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) bean.getClass();
		UpdateSQL<T> updateSQL = new UpdateSQL<T>();
		updateSQL.primaryKey = new CompoundPrimaryKey(bean);
		updateSQL.beanClass = beanClass;
		updateSQL.setTableName();
		//初始化条件
		int keyCount = updateSQL.primaryKey.getCount();
		String[] keyNames = updateSQL.primaryKey.getNames();
		Object[] keyValues = updateSQL.primaryKey.getValues();
		for (int i = 0; i < keyCount; i++) {
			updateSQL.andEq(keyNames[i], keyValues[i]);
		}
		
		return updateSQL;
	}
	
	/**
	 * 初始化
	 */
	private void init0(T bean) {
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(beanClass)
				.getPdsWithAnnotation(XLPColumn.class);
		int len = pds.length;
		XLPColumn xlpColumn = null;
		Class<?> filedTypeClass;
		for (int i = 0; i < len; i++) {
			xlpColumn = pds[i].getFieldAnnotation(XLPColumn.class);
			String colName = xlpColumn.columnName();
			Object value = BeanUtil.callGetter(bean, pds[i]);
			if((value == null || (pds[i].getFiledClassType().isPrimitive() &&
					value.toString().equals("0"))) && !allUpdate)
				continue;
			columnNames.add(colName);
			filedTypeClass = pds[i].getFiledClassType();
			if(String.class.equals(filedTypeClass) && value != null){
				if (xlpColumn.trim()) {
					value = ((String)value).trim();
				}
				int maxLen = xlpColumn.maxLength();
				value = XLPStringUtil.getSuitLenString((String)value, maxLen);
			}
			updateValues.add(value);
		}
		//初始化条件
		int keyCount = primaryKey.getCount();
		String[] keyNames = primaryKey.getNames();
		Object[] keyValues = primaryKey.getValues();
		for (int i = 0; i < keyCount; i++) {
			andEq(keyNames[i], keyValues[i]);
		}
	}

	/**
	 * 设置要更新的字段
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return UpdateSQL对象
	 */
	public UpdateSQL<T> set(String fieldName, Object value){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		columnNames.add(colName);
		updateValues.add(value);
		return this;
	}
	
	/**
	 * 清除所有要更新的列
	 * 
	 * @return
	 */
	public UpdateSQL<T> clearUpdate() {
		columnNames.clear();
		updateValues.clear();
		return this;
	}

	/**
	 * 清除指定的更新列
	 * 
	 * @param fieldName
	 *            可以是实体的字段名，也可以数据库中的列名称
	 * @return
	 */
	public UpdateSQL<T> removeUpdate(String fieldName) {
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
			colName = (colName == null ? fieldName : colName);
			int size = columnNames.size();
			for (int i = 0; i < size; i++) {
				if(colName.equals(columnNames.get(i))){
					columnNames.remove(i);
					updateValues.remove(i);
					break;
				}
			}
		}
		return this;
	}
	
	@Override
	protected void init(T bean) throws EntityException {
		
	}
	
	@Override
	public String getParamSql() {
		int size = updateValues.size();
		if(size < 1)
			return null;
		
		String tableName = getTableName();
		StringBuilder pre = new StringBuilder("update ").append(tableName)
			.append(" set ");
		for (int i = 0; i < size; i++) {
			if(i != 0)
				pre.append(COMMA);
			pre.append(tableName).append(".")
				.append(columnNames.get(i)).append("=?");
		}
		
		String sql = pre.append(partSql).toString();
		LOGGER.debug("形成的更新SQL语句是：" + sql);
		return sql;
	}

	@Override
	public String getSql() {
		int size = updateValues.size();
		if(size < 1)
			return null;
		
		String tableName = getTableName();
		StringBuilder pre = new StringBuilder("update ").append(tableName)
			.append(" set ");
		
		for (int i = 0; i < size; i++) {
			if(i != 0)
				pre.append(COMMA);
			pre.append(tableName).append(".")
				.append(columnNames.get(i)).append("=");
			Object value = null;
			if ((value = updateValues.get(i)) == null){
				pre.append("null");
			}else{
				if(value instanceof CharSequence || value instanceof Character)
					pre.append(SINGLE_QUOTE).append(value)
						.append(SINGLE_QUOTE);
				else if (value.getClass().equals(Boolean.class))
					if((Boolean)value)
						pre.append(1);
					else
						pre.append(0);
				else
					pre.append(value);
			}
		}
		String sql = pre.append(partSqlToString()).toString();
		LOGGER.debug("形成的更新SQL语句是：" + sql);
		return sql;
	}

	@Override
	public Object[] getParams() {
		 updateValues.addAll(valueList);
		 return updateValues.toArray();
	}
}
