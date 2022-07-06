package org.xlp.db.sql.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPForeign;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.utils.XLPStringUtil;

/**
 * 表对象信息
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-9
 *         </p>
 * @version 1.0
 */
public class Table<T> {
	private String tableName; //表名
	private String alias; //表别名
	/**
	 * 表所有所有字段数组（主键字段在最前）即对应表中的字段名
	 */
	private String[] allColumnNames;
	/**
	 * 表所有所有字段数组除key对应字段外，即对应表中的字段名
	 */
	private String[] columnNames;
	
	/**
	 * 对应的实体类对象 
	 */
	private Class<T> entityClass;
	
	/**
	 * 存储主键对应的字段
	 */
	private List<Field> primaryFields = new ArrayList<Field>();
	
	/**
	 * 存储外键对应的字段
	 */
	private List<Field> foreignFields = new ArrayList<Field>();
	
	/**
	 * 实体字段名与数据库表列名映射关系 key:实体字段名,value数据库表列名
	 */
	private Map<String, String> beanFieldNameMapperDbColumnNameMap = new HashMap<String, String>(); 
	
	/**
	 * 以实体类型构建此对象
	 * 
	 * @param entityClass
	 * @throws EntityException 
	 */
	public Table(Class<T> entityClass) throws EntityException{
		this.entityClass = entityClass;
		initData(entityClass);
	}

	/**
	 * 数据初始化
	 * 
	 * @param entityClass
	 * @throws EntityException 
	 */
	private void initData(Class<T> entityClass) throws EntityException {
		this.tableName = SQLUtil.getTableName(entityClass);
		if (this.tableName == null)
			throw new EntityException(entityClass.getName() + ": 没有XLPEntity实体注解");
		
		JavaBeanPropertiesDescriptor<T> jbp = new JavaBeanPropertiesDescriptor<T>(entityClass);
		PropertyDescriptor<T>[] kPds = jbp.getPdsWithAnnotation(XLPId.class);//key属性字段描述
	
		PropertyDescriptor<T>[] pds = jbp.getPdsWithAnnotation(XLPColumn.class);//普通属性字段描述
		int len = pds.length;
		int kLen = kPds.length;
		allColumnNames = new String[len + kLen];
		columnNames = new String[len];
		
		int i;
		for (i = 0; i < kLen; i++) {
			allColumnNames[i] = kPds[i].getFieldAnnotation(XLPId.class)
				.columnName();
			allColumnNames[i] = XLPStringUtil.isEmpty(allColumnNames[i]) ? 
					kPds[i].getFieldName() : allColumnNames[i];
			primaryFields.add(kPds[i].getField());
			beanFieldNameMapperDbColumnNameMap.put(kPds[i].getFieldName(), allColumnNames[i]);
		}
		
		String colName = null;
		for (int j = 0; j < len; j++, i++) {
			colName = pds[j].getFieldAnnotation(XLPColumn.class).columnName();
			colName = XLPStringUtil.isEmpty(colName) ? pds[j].getFieldName() : colName;
			allColumnNames[i] = colName; //初始化表所有所有字段数组（主键字段在最前）
			columnNames[j] = colName; //表所有所有字段数组除key对应字段外
			beanFieldNameMapperDbColumnNameMap.put(pds[j].getFieldName(), colName);
		}
		
		pds = jbp.getPdsWithAnnotation(XLPForeign.class);//外键属性字段描述
		for (PropertyDescriptor<T> pd : pds) {
			foreignFields.add(pd.getField());
		}
	}

	public String getAlias() {
		return alias;
	}

	/**
	 * 给表设置别名 
	 * 
	 * @param alias
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String[] getAllColumnNames() {
		return allColumnNames;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public String getTableName() {
		return tableName;
	}
	
	/**
	 * @return the primaryFields
	 */
	public List<Field> getPrimaryFields() {
		return primaryFields;
	}

	/**
	 * @return the foreignFields
	 */
	public List<Field> getForeignFields() {
		return foreignFields;
	}

	/**
	 * @return the entityClass
	 */
	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	/**
	 * 实体字段名与数据库表列名映射关系 key:实体字段名,value数据库表列名
	 * 
	 * @return
	 */
	public Map<String, String> getBeanFieldNameMapperDbColumnNameMap() {
		return beanFieldNameMapperDbColumnNameMap;
	}
}
