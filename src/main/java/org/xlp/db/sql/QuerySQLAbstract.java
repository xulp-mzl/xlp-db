package org.xlp.db.sql;


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
public abstract class QuerySQLAbstract<T> extends OneTableSQLAbstract<T>{
	//记录是否已排序
	private boolean isSorted = false;
	//是否分组
	private boolean isGroup = false;
	
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
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param orderType 排序方式（asc | desc）
	 * @return
	 */
	private QuerySQLAbstract<T> orderBy(String fieldName, String orderType){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		if(!isSorted){
			partSql.append(" order by ");
			isSorted = true;
		}else
			partSql.append(COMMA);
		partSql.append(getTableName()).append(".")
			.append(colName).append(" ").append(orderType).append(" ");
		return this;
	}
	
	/**
	 * 排序升序
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> asc(String fieldName){
		return orderBy(fieldName, ASC);
	}
	
	/**
	 * 排序降序
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> desc(String fieldName){
		return orderBy(fieldName, DESC);
	}

	/**
	 * group by
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public QuerySQLAbstract<T> groupBy(String fieldName){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		if(!isGroup){
			partSql.append("group by ");
			isGroup = true;
		} else
			partSql.append(COMMA);
		partSql.append(getTableName()).append(".").append(colName).append(" ");
		return this;
	}
}
