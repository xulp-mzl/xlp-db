package org.xlp.db.sql.statisticsfun;

/**
 * <p>创建时间：2022年5月15日 上午12:19:18</p>
 * @author xlp
 * @version 1.0 
 * @Description SQL集中函数的统计类型
*/
public interface SQLStatisticsType {
	/**
	 * 统计函数的别名
	 * 
	 * @return
	 */
	public String getAlias();
	
	/**
	 * 设置统计函数的别名
	 * 
	 * @return
	 */
	public void setAlias(String alias);
	
	/**
	 * 获取统计函数部分SQL片段
	 * 
	 * @return
	 */
	public String getStatisticsPartSql();
}
