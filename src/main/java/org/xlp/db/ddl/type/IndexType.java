package org.xlp.db.ddl.type;
/**
 * <p>创建时间：2021年3月24日 下午10:08:39</p>
 * @author xlp
 * @version 1.0 
 * @Description 数据库字段索引类型
*/
public enum IndexType {
	/**
	 * 普通索引
	 */
	NORMAL,
	
	/**
	 * 唯一索引
	 */
	UNIQUE,
	
	/**
	 * 全文索引
	 */
	FULLTEXT
}
