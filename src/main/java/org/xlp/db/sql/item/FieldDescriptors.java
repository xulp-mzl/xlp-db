package org.xlp.db.sql.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2022年8月26日 下午10:04:19</p>
 * @author xlp
 * @version 1.0 
 * @Description SQL查询字段描述器集合
*/
public class FieldDescriptors {
	/**
	 * 存储SQL查询字段别名
	 */
	private Set<String> aliasSet = new HashSet<>();
	
	/***
	 * 存储bean字段名称与表列名映射关系
	 */
	private Map<String, String> fieldColumnMap = new HashMap<String, String>();
	
	/**
	 * 添加别名
	 * @param alias
	 */
	public void addAlias(String alias){
		if (XLPStringUtil.isEmpty(alias)) {
			aliasSet.add(alias);
		}
	}
	
	/**
	 * 判断是否存在指定的别名
	 * @param alias
	 * @return true：存在，false：不存在
	 */
	public boolean hasAlias(String alias){
		return aliasSet.contains(alias);
	}
	
	/**
	 * 添加字段名称与类名映射关系
	 * @param fieldName 字段名称
	 * @param columnName 表列名
	 */
	public void putFieldColumn(String fieldName, String columnName){
		if (!XLPStringUtil.isEmpty(fieldName) &&
				!XLPStringUtil.isEmpty(columnName)) {
			fieldColumnMap.put(fieldName, columnName);
		}
	}
	
	/**
	 * 从缓存中根据字段名获取表列名
	 * @param fieldName
	 * @return
	 */
	public String getColumnName(String fieldName){
		return fieldColumnMap.get(fieldName);
	}
	
	/**
	 * 判断是否存在字段名对应的列名
	 * 
	 * @param fieldName 字段名
	 * @return true：存在，false：不存在
	 */
	public boolean hasColumnName(String fieldName){
		return !XLPStringUtil.isEmpty(getColumnName(fieldName));
	}
}
