package org.xlp.db.sql.statisticsfun;

import org.xlp.db.sql.SQL;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.sql.table.Table;
import org.xlp.utils.XLPArrayUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2022年5月15日 下午5:46:24</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public class DistinctCount extends SQLStatisticsAbstract{
	/**
	 * 是否去重 
	 */
	private String[] distinctFields;
	
	public DistinctCount() {
		super();
	}

	public DistinctCount(Table<?> table, String fieldName, String alias) {
		super(table, fieldName, alias);
	}

	public DistinctCount(Table<?> table, String fieldName) {
		super(table, fieldName);
	}
	
	public DistinctCount(Table<?> table, String[] distinctFields, String alias) {
		super(table, null, alias);
		this.distinctFields = distinctFields;
	}

	public DistinctCount(Table<?> table, String[] distinctFields) {
		this(table, distinctFields, null);
	}

	@Override
	public String getStatisticsPartSql() {
		if (!XLPArrayUtil.isEmpty(distinctFields)) {
			String tableAlias = SQLUtil.getTableAlias(getTable());
			StringBuilder sb = new StringBuilder();
			sb.append("count(distinct ");
			for (int i = 0; i < distinctFields.length; i++) { 
				if (i != 0) {
					sb.append(SQL.COMMA);
				}
				sb.append(tableAlias).append(distinctFields[i]);
			}
			sb.append(SQL.RIGHT_BRACKET);
			return sb.toString();
		} else if (XLPStringUtil.isEmpty(getFieldName())) {
			return "count(*)";
		} else {
			return getStatisticsPartSql("count");
		}
	}

	/**
	 * @return the distinctFields
	 */
	public String[] getDistinctFields() {
		return distinctFields;
	}

	/**
	 * @param distinctFields the distinctFields to set
	 */
	public void setDistinctFields(String... distinctFields) {
		this.distinctFields = distinctFields;
	}
}
