package org.xlp.db.sql;

import org.xlp.db.sql.table.Table;

/**
 * <p>创建时间：2022年5月21日 下午10:58:26</p>
 * @author xlp
 * @version 1.0 
 * @Description 可查询SQL对象接口
*/
public interface Query extends SQL{
	/**
	 * 获取查询SQL对象对应的标对象
	 * 
	 * @return
	 */
	public Table<?> getTable();
}
