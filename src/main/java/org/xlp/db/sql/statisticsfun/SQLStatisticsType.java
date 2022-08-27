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
	 * 获取统计函数名称，如：svg([column])
	 * 
	 * @return
	 */
	public String getSQLMenthodName();
	
	/**
	 * 获取要统计字段的名称
	 * @return
	 */
	public String getFieldName();
}
