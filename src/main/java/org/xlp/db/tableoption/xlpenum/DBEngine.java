package org.xlp.db.tableoption.xlpenum;
/**
 * <p>创建时间：2021年3月17日 下午10:58:09</p>
 * @author xlp
 * @version 1.0 
 * @Description 数据库引擎枚举类
*/
public enum DBEngine {
	INNODB("INNODB"),
	ARCHIVE("ARCHIVE"),
	CSV("CSV"),
	BLACKHOLE("BLACKHOLE"),
	MEMORY("MEMORY"),
	MYISAM("MYISAM")
	;
	
	/**
	 * 数据库引擎名称
	 */
	private String dbEngineName;
	
	private DBEngine(String dbEngineName){
		this.dbEngineName = dbEngineName;
	}

	/**
	 * @return 数据库引擎名称
	 */
	public String getDbEngineName() {
		return dbEngineName;
	}
}
