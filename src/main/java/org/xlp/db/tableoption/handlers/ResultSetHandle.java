package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集处理结果接口
 * 
 * @author 徐龙平
 *         <p>
 *         2017-1-3
 *         </p>
 * @version 1.0
 * 
 */
public interface ResultSetHandle<T> {
	public T handle(ResultSet rs) throws SQLException;
}
