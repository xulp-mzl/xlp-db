package org.xlp.db.sql.statisticsfun;

/**
 * <p>创建时间：2022年5月15日 下午5:46:24</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public class Avg extends SQLStatisticsAbstract{
	public Avg() {
		super();
	}

	public Avg( String fieldName, String alias) {
		super(fieldName, alias);
	}

	public Avg(String fieldName) {
		super(fieldName);
	}
	
	@Override
	public String getSQLMenthodName() {
		return "avg(%s)";
	}
}
