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
		setStartPos(startPos, resultCount);
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos, long resultCount) {
		this.startPos = startPos;
		this.resultCount = resultCount;
		limit(new Limit(startPos, resultCount));
	}

	public long getResultCount() {
		return resultCount;
	}
}
