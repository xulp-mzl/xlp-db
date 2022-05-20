package org.xlp.db.sql.limit;

import org.xlp.db.tableoption.xlpenum.DBType;
import org.xlp.db.utils.XLPDBUtil;

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
	//数据库类型
	private DBType dbType;
	//数据库名称
	private static final String MYSQL = "MySQL";
	
	protected Limit(){
		//setDbType();
	}

	/**
	 * 构造函数
	 * 
	 * @param startPos 分页起始位置，从0开始
	 * @param resultCount 每页数量
	 */
	public Limit(long startPos, long resultCount) {
		this.startPos = startPos;
		this.resultCount = resultCount;
		//setDbType();
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getResultCount() {
		return resultCount;
	}

	public void setResultCount(long resultCount) {
		this.resultCount = resultCount;
	}

	public DBType getDbType() {
		return dbType;
	}

	protected void setDbType() {
		String dbName = XLPDBUtil.getDatabaseProductName();
		if (MYSQL.equalsIgnoreCase(dbName)) {
			this.dbType = DBType.MYSQL_DB;
		}
		
	}

	@Override
	public String toString() {
		return "Limit [dbType=" + dbType + ", resultCount=" + resultCount
				+ ", startPos=" + startPos + "]";
	}
}
