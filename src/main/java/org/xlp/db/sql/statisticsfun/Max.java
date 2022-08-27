package org.xlp.db.sql.statisticsfun;

/**
 * <p>创建时间：2022年5月15日 下午5:46:24</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public class Max extends SQLStatisticsAbstract{
	public Max() {
		super();
	}

	public Max(String fieldName, String alias) {
		super(fieldName, alias);
	}

	public Max(String fieldName) {
		super(fieldName);
	}

	@Override
	public String getSQLMenthodName() {
		return "max(%s)";
	}
}
