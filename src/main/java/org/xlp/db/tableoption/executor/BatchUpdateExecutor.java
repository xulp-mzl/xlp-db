package org.xlp.db.tableoption.executor;

import java.sql.SQLException;

/**
 * 批量更新操作做执行器接口
 * <p>用来批量执行更新SQL语句
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public interface BatchUpdateExecutor{
	/**
	 * 批量执行sql
	 * 
	 * @param sql
	 * @param params 预处理参数，假如不需要此参数，请给出一个大小为0的数组，不要填null
	 * @return 更新数据的条数
	 * @throws SQLException
	 */
	public int[] execute(String sql, Object[][] params) throws SQLException;
}
