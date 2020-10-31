package org.xlp.db.sql.table;

import java.util.Arrays;

import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;

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
	private String[] allColumnNames;//表所有所有字段数组（主键字段在最前）
	private String[] columnNames;//表所有所有字段数组除key对应字段外
	
	/**
	 * 以实体类型构建此对象
	 * 
	 * @param entityClass
	 * @throws EntityException 
	 */
	public Table(Class<T> entityClass) throws EntityException{
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
		}
		
		String colName = null;
		for (int j = 0; j < len; j++, i++) {
			colName = pds[j].getFieldAnnotation(XLPColumn.class)
				.columnName();
			allColumnNames[i] = colName; //初始化表所有所有字段数组（主键字段在最前）
			columnNames[j] = colName; //表所有所有字段数组除key对应字段外
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

	@Override
	public String toString() {
		return "Table [alias=" + alias + ", allColumnNames="
				+ Arrays.toString(allColumnNames) + ", columnNames="
				+ Arrays.toString(columnNames) + ", tableName=" + tableName
				+ "]";
	}
	
}
