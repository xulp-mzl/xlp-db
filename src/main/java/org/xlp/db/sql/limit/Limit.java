package org.xlp.db.sql.limit;

/**
 * limit类，存储分页查询的基本信息
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-15
 *         </p>
 * @version 1.0
 */
public class Limit {
	/**
	 * 分页起始位置，从0开始
	 */
	private long startPos;
	/**
	 * 每页数量
	 */
	private long resultCount;
	
	/**
	 * 构造函数
	 * 
	 * @param startPos 分页起始位置，从0开始
	 * @param resultCount 每页数量
	 */
	public Limit(long startPos, long resultCount) {
		this.startPos = startPos;
		this.resultCount = resultCount;
	}

	public long getStartPos() {
		return startPos;
	}

	public long getResultCount() {
		return resultCount;
	}
}
