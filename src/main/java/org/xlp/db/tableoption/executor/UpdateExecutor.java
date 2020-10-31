package org.xlp.db.tableoption.executor;

import java.sql.SQLException;

/**
 * 更新操作做执行器接口
 * <p>用来执行更新SQL语句
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public interface UpdateExecutor {
	/**
	 * 执行sql
	 * 
	 * @param sql
	 * @return 更新数据的条数
	 * @throws SQLException
	 */
	public int execute(String sql) throws SQLException;
	
	/**
	 * 执行sql
	 * 
	 * @param sql
	 * @param params
	 * @return 更新数据的条数
	 * @throws SQLException
	 */
	public int execute(String sql, Object... params) throws SQLException;
}
