package org.xlp.db.tableoption.handlers.result;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;


import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.annotation.XLPIrrelevantColumn;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.javabean.processer.ValueProcesser;
import org.xlp.utils.XLPStringUtil;

/**
 * 结果集处理成不同的结果对象的类，与相关注解一起使用
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 3.0
 * 
 */
public class ResultWithAnnotation extends DefaultResult{
	
	public ResultWithAnnotation() {
		super();
	}

	/**
	 * @param format 字符串与时间的转换格式
	 */
	public ResultWithAnnotation(String format) {
		super(format);
	}
	
	/**
	 * 值处理器
	 * @param processer
	 */
	public ResultWithAnnotation(ValueProcesser processer){
		super(processer);
	}

	protected <T> int[] columnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor<T>[] pds) 
			throws SQLException {
		int cols = rsmd.getColumnCount();
		int[] columnToProperty = new int[cols + 1];
	    Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);
	    
	    XLPColumn xlpColumn;
	    XLPId xlpId;
	    XLPIrrelevantColumn irrelevantColumn;
	    String columnName, columnLabel, colName = null;
	    for (int col = 1; col <= cols; col++) {
			columnName = rsmd.getColumnName(col);
			columnLabel = rsmd.getColumnLabel(col);
			
			for (int i = 0, len = pds.length; i < len; i++) {
				//得到此字段上的带指定注解类的注解
				xlpColumn = pds[i].getFieldAnnotation(XLPColumn.class);
				if (xlpColumn != null) {
					//得到注解中columnName的名称
					colName = xlpColumn.columnName().trim();
					colName = XLPStringUtil.isEmpty(colName) ? pds[i].getFieldName() : colName;
				}else if((xlpId = pds[i].getFieldAnnotation(XLPId.class)) != null){
					colName = xlpId.columnName().trim();
					colName = XLPStringUtil.isEmpty(colName) ? pds[i].getFieldName() : colName;
				}else if((irrelevantColumn = pds[i].getFieldAnnotation(XLPIrrelevantColumn.class)) != null){
					colName = irrelevantColumn.columnName().trim();
					colName = XLPStringUtil.isEmpty(colName) ? pds[i].getFieldName() : colName;
				}

				//判断属性名称与表列名是否相同
				if (colName != null && (colName.equalsIgnoreCase(columnName)
						|| colName.equalsIgnoreCase(columnLabel))) {
					columnToProperty[col] = i;
					break;
				}
			}
		}
	    
	    return columnToProperty;
	}
}
