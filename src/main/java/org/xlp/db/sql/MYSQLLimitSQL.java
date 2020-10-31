package org.xlp.db.sql;


import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.limit.Limit;

/**
 * 可含条件单表分页数据查询SQL信息类（只针对mysql数据库有效）
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-14
 *         </p>
 * @version 1.0
 */
public class MYSQLLimitSQL<T> extends QuerySQL<T>{
	//分页开始位置
	private long startPos;
	//记录条数
	private long resultCount;
	
	public MYSQLLimitSQL(Class<T> beanClass) throws EntityException {
		super(beanClass);
	}
	
	public MYSQLLimitSQL(Class<T> beanClass, long startPos
			,long resultCount) throws EntityException {
		super(beanClass);
		this.startPos = startPos;
		this.resultCount = resultCount;
	}

	@Override
	public String getSql() {
		String alias = getTable().getAlias();
		partSql.append(" limit ?,?");
		
		String partSql0 = partSqlToString();
		String tableName = getTableName();
		alias = (alias == null ? tableName + "_0" : alias);
		
		partSql0 = partSql0.replace(tableName+".", alias+".");
		String sql = preSql().append(partSql0).toString();
		LOGGER.debug("形成的查询SQL语句是：" + sql);
		return sql;
	}
	
	@Override
	public String getParamSql() {
		String alias = getTable().getAlias();
		partSql.append(" limit ?,?");
		
		String partSql0 = partSql.toString();
		String tableName = getTableName();
		alias = (alias == null ? tableName + "_0" : alias);
		
		partSql0 = partSql0.replace(tableName+".", alias+".");
		String sql = preSql().append(partSql0).toString();
		LOGGER.debug("形成的查询SQL语句是：" + sql);
		return sql;
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getResultCount() {
		return resultCount;
	}

	public void setResultCount(long resultCount) {
		this.resultCount = resultCount;
	}
	
	@Override
	public Object[] getParams() {
		valueList.add(startPos);
		valueList.add(resultCount);
		return super.getParams();
	}
	
	@Override
	public QuerySQL<T> limit(Limit limit) throws EntityException {
		return this;
	}
}
