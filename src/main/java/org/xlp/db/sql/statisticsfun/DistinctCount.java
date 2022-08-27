package org.xlp.db.sql.statisticsfun;

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

	public DistinctCount(String fieldName, String alias) {
		super(fieldName, alias);
	}

	public DistinctCount(String fieldName) {
		super(fieldName);
	}
	
	public DistinctCount(String[] distinctFields, String alias) {
		super(null, alias);
		this.distinctFields = distinctFields;
	}

	public DistinctCount(String[] distinctFields) {
		this(distinctFields, null);
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

	@Override
	public String getSQLMenthodName() {
		return "count(%s)";
	}
	
	
}
