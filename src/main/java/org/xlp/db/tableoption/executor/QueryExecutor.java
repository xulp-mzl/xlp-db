package org.xlp.db.tableoption.executor;

import java.sql.SQLException;

import org.xlp.db.tableoption.OriginalResultSetOption;

/**
 * 查询操作做执行器接口
 * <p>用来执行查询SQL语句
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public interface QueryExecutor {
	/**
	 * 执行查询SQL
	 * 
	 * @param sql
	 * @return 返回<code>OriginalResultSetOption</code>对像，使用完此对象要关闭哦
	 * @throws SQLException
	 */
	public OriginalResultSetOption execute(String sql) throws SQLException;
	
	/**
	 * 执行查询SQL
	 * 
	 * @param sql
	 * @param params
	 * @return 返回<code>OriginalResultSetOption</code>对像，使用完此对象要关闭哦
	 * @throws SQLException
	 */
	public OriginalResultSetOption execute(String sql, Object... params) 
			throws SQLException;
}
