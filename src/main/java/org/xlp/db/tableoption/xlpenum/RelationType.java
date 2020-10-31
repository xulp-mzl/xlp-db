package org.xlp.db.tableoption.xlpenum;

/**
 * 关系类型
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-1
 *         </p>
 * @version 1.0
 * 
 */
public enum RelationType {
	/**
	 * 一对一
	 */
	ONE,
	
	/**
	 * 一对多（list）
	 */
	LIST,
	
	/**
	 * 一对多（set）
	 */
	SET
}
