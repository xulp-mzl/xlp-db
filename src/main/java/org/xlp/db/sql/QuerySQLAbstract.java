package org.xlp.db.sql;


import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xlp.db.exception.EntityException;
import org.xlp.db.utils.BeanUtil;

/**
 * 含条件单表SQL查询信息抽象类
 * <p>注意：这个类的子类的构造方法中应该先调用父类的构造方法，在执行别的内容，否则可能会抛出异常
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-07
 *         </p>
 * @version 1.0
 */
public abstract class QuerySQLAbstract<T> extends OneTableSQLAbstract<T> implements Query{
	/**
	 * 存储排序字段
	 */
	private Map<String, String> sortFields = new LinkedHashMap<String, String>();
	
	/**
	 * 存储分组字段
	 */
	private Set<String> groupFields = new LinkedHashSet<String>();
	
	public QuerySQLAbstract(Class<T> beanClass) throws EntityException {
		super(beanClass);
	}

	public QuerySQLAbstract(T bean) throws EntityException {
		super(bean);
	}
	
	protected QuerySQLAbstract(){
		super();
	}
	
	/**
	 * order by
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param orderType 排序方式（asc | desc）
	 * @return
	 */
	private QuerySQLAbstract<T> orderBy(String fieldName, String orderType){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
		sortFields.put(colName, orderType);
		
		return this;
	}
	
	/**
	 * 排序升序
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> asc(String fieldName){
		return orderBy(fieldName, ASC);
	}
	
	/**
	 * 排序降序
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> desc(String fieldName){
		return orderBy(fieldName, DESC);
	}

	/**
	 * group by
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> groupBy(String fieldName){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
		groupFields.add(colName);
		return this;
	}
	
	/**
	 * 分组排序sql片段
	 * 
	 * @return
	 */
	protected String formatterGroupByAndOrderBySql(){
		String tableAlias = SQLUtil.getTableAlias(getTable());
		
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		if (!groupFields.isEmpty()) {
			sb.append(" group by ");
			for (String group : groupFields) {
				if (!start) {
					sb.append(COMMA).append(" ");
				}
				sb.append(tableAlias).append(group);
				start = false;
			}
		}
		if (!sortFields.isEmpty()) {
			sb.append(" order by ");
			start = true;
			for (Entry<String, String> sort : sortFields.entrySet()) {
				if (!start) {
					sb.append(COMMA).append(" ");
				}
				sb.append(tableAlias).append(sort.getKey())
					.append(" ").append(sort.getValue());
				start = false;
			}
		}
		return sb.toString();
	}
}
