package org.xlp.db.sql.statisticsfun;

import org.xlp.db.sql.table.Table;

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

	public Max(Table<?> table, String fieldName, String alias) {
		super(table, fieldName, alias);
	}

	public Max(Table<?> table, String fieldName) {
		super(table, fieldName);
	}

	@Override
	public String getStatisticsPartSql() {
		return getStatisticsPartSql("max");
	}
}
