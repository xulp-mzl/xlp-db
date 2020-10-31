package org.xlp.db.tableoption.handlers.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 结果集处理成不同的结果对象
 * 
 * @author 徐龙平
 *         <p>
 *         2017-1-3
 *         </p>
 * @version 1.0
 * 
 */
public interface Result {
	/**
	 * 处理成javabean
	 * 
	 * @param rs
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public <T> T toJavaBean(ResultSet rs, Class<T> type) throws SQLException;

	/**
	 * 处理成javabean List集合
	 * 
	 * @param rs
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public <T> List<T> toJavaBeanList(ResultSet rs, Class<T> type)
			throws SQLException;

	/**
	 * 处理成Map集合
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Object> toMap(ResultSet rs) throws SQLException;

	/**
	 * 处理成Map List集合
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> toMapList(ResultSet rs)
			throws SQLException;

	/**
	 * 把每行记录处理成数组
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public Object[] toArray(ResultSet rs) throws SQLException;

	/**
	 * 把每行记录处理成数组 List集合
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public List<Object[]> toArrayList(ResultSet rs) throws SQLException;

	/**
	 * 处理成javabean Set集合
	 * 
	 * @param rs
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public <T> Set<T> toJavaBeanSet(ResultSet rs, Class<T> type)
			throws SQLException;
}
